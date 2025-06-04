package com.example.arpublicservices

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import kotlinx.coroutines.*
import android.view.View

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var arSceneView: ArSceneView
    private lateinit var webServiceManager: WebServiceManager
    private lateinit var serviceFilterManager: ServiceFilterManager
    private lateinit var arRenderer: ARRenderer
    private var currentLocation: Location? = null
    private val publicServices = mutableListOf<PublicService>()
    private var isARMode = false
    private val arNodes = mutableListOf<Node>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2
        private const val TAG = "ARPublicServices"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeComponents()
        initializeManagers()
        checkPermissions()
        checkArCoreSupport()
        setupFAB()
    }

    private fun initializeComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        arSceneView = findViewById(R.id.ar_scene_view)
    }

    private fun initializeManagers() {
        webServiceManager = WebServiceManager()
        serviceFilterManager = ServiceFilterManager()
        arRenderer = ARRenderer(this)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun checkArCoreSupport() {
        when (ArCoreApk.getInstance().checkAvailability(this)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
                Log.d(TAG, "ARCore está instalado y soportado")
            }
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                ArCoreApk.getInstance().requestInstall(this, true)
            }
            else -> {
                Toast.makeText(this, "ARCore no es compatible con este dispositivo", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true

        googleMap.setOnCameraMoveListener {
            loadPublicServicesInArea()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    loadPublicServicesInArea()
                    setupARView()
                }
            }
        }
    }

    private fun loadPublicServicesInArea() {
        val bounds = googleMap.projection.visibleRegion.latLngBounds
        val mapBounds = MapBounds(bounds.southwest, bounds.northeast)

        publicServices.clear()
        googleMap.clear()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val electricalServices = async(Dispatchers.IO) {
                    webServiceManager.loadElectricalInfrastructure(mapBounds)
                }
                val waterServices = async(Dispatchers.IO) {
                    webServiceManager.loadWaterInfrastructure(mapBounds)
                }
                val gasServices = async(Dispatchers.IO) {
                    webServiceManager.loadGasInfrastructure(mapBounds)
                }
                val telecomServices = async(Dispatchers.IO) {
                    webServiceManager.loadTelecommunicationsInfrastructure(mapBounds)
                }

                val allServices = mutableListOf<PublicService>()
                allServices.addAll(electricalServices.await())
                allServices.addAll(waterServices.await())
                allServices.addAll(gasServices.await())
                allServices.addAll(telecomServices.await())

                addServicesToMap(allServices)
                publicServices.addAll(allServices)

                if (isARMode) {
                    setupARView()
                }

                updateServicesRecyclerView()

            } catch (e: Exception) {
                Log.e(TAG, "Error cargando servicios", e)
                Toast.makeText(this@MainActivity, "Error cargando servicios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addServicesToMap(services: List<PublicService>) {
        services.forEach { service ->
            val position = LatLng(service.latitude, service.longitude)
            val markerColor = getMarkerColorForType(service.type)

            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(service.name)
                    .snippet(service.description)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
        }
    }

    private fun setupARView() {
        currentLocation?.let { location ->
            clearARNodes()

            val arServices = publicServices.filter { service ->
                arRenderer.shouldShowInAR(service, location)
            }

            arServices.forEach { service ->
                arRenderer.createServiceNode(service, location)
                    .thenAccept { node ->
                        runOnUiThread {
                            arSceneView.scene.addChild(node)
                            arNodes.add(node)
                        }
                    }
                    .exceptionally { throwable ->
                        Log.e(TAG, "Error agregando nodo AR", throwable)
                        null
                    }
            }
        }
    }

    private fun clearARNodes() {
        arNodes.forEach { node ->
            arSceneView.scene.removeChild(node)
        }
        arNodes.clear()
    }

    private fun updateServicesRecyclerView() {
        currentLocation?.let { location ->
            val sortedServices = serviceFilterManager.sortServicesByDistance(publicServices, location)
            val adapter = PublicServiceAdapter(sortedServices)
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.services_recycler_view).adapter = adapter
        }
    }

    private fun setupFAB() {
        val fabToggleAR = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_toggle_ar)
        fabToggleAR.setOnClickListener {
            toggleARMode()
        }
    }

    private fun toggleARMode() {
        isARMode = !isARMode

        if (isARMode) {
            arSceneView.visibility = View.VISIBLE
            // Cambio: obtener la vista del fragment correctamente
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            mapFragment?.view?.alpha = 0.3f
            setupARView()
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_toggle_ar)
                .setImageResource(R.drawable.ic_map)
        } else {
            arSceneView.visibility = View.GONE
            // Cambio: obtener la vista del fragment correctamente
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            mapFragment?.view?.alpha = 1.0f
            clearARNodes()
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_toggle_ar)
                .setImageResource(R.drawable.ic_ar_camera)
        }
    }

    private fun getMarkerColorForType(type: String): Float {
        return when (type) {
            "hospital" -> BitmapDescriptorFactory.HUE_RED
            "school" -> BitmapDescriptorFactory.HUE_BLUE
            "police" -> BitmapDescriptorFactory.HUE_CYAN
            "fire_station" -> BitmapDescriptorFactory.HUE_ORANGE
            "post_office" -> BitmapDescriptorFactory.HUE_YELLOW
            "bank" -> BitmapDescriptorFactory.HUE_GREEN
            "electrical_substation" -> BitmapDescriptorFactory.HUE_VIOLET
            "water_treatment" -> BitmapDescriptorFactory.HUE_AZURE
            "gas_station" -> BitmapDescriptorFactory.HUE_ROSE
            "cell_tower" -> BitmapDescriptorFactory.HUE_MAGENTA
            else -> BitmapDescriptorFactory.HUE_VIOLET
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Permisos de ubicación requeridos", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        arSceneView.resume()
    }

    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }
}