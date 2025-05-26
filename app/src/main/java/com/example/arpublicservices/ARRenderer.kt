package com.example.arpublicservices

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.TextView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture
import kotlin.math.*

class ARRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "ARRenderer"
        private const val MAX_AR_DISTANCE = 2000f
        private const val SCALE_FACTOR = 50f
    }
    
    fun createServiceNode(
        service: PublicService, 
        userLocation: Location
    ): CompletableFuture<Node> {
        val future = CompletableFuture<Node>()
        
        ViewRenderable.builder()
            .setView(context, R.layout.ar_service_info)
            .build()
            .thenAccept { renderable ->
                try {
                    val view = renderable.view
                    view.findViewById<TextView>(R.id.service_name_ar).text = service.name
                    view.findViewById<TextView>(R.id.service_type_ar).text = 
                        getServiceTypeDisplayName(service.type)
                    
                    val node = Node().apply {
                        this.renderable = renderable
                        localPosition = calculateARPosition(service, userLocation)
                        localScale = Vector3(0.5f, 0.5f, 0.5f)
                    }
                    
                    future.complete(node)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error creando nodo AR para servicio ${service.name}", e)
                    future.completeExceptionally(e)
                }
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Error construyendo renderable", throwable)
                future.completeExceptionally(throwable)
                null
            }
        
        return future
    }
    
    private fun calculateARPosition(service: PublicService, userLocation: Location): Vector3 {
        val serviceLocation = Location("").apply {
            latitude = service.latitude
            longitude = service.longitude
        }
        
        val distance = userLocation.distanceTo(serviceLocation)
        val bearing = userLocation.bearingTo(serviceLocation)
        
        val clampedDistance = minOf(distance, MAX_AR_DISTANCE)
        
        val x = (clampedDistance * sin(Math.toRadians(bearing.toDouble()))).toFloat() / SCALE_FACTOR
        val z = -(clampedDistance * cos(Math.toRadians(bearing.toDouble()))).toFloat() / SCALE_FACTOR
        val y = getServiceHeight(service.type)
        
        return Vector3(x, y, z)
    }
    
    private fun getServiceHeight(serviceType: String): Float {
        return when (serviceType.lowercase()) {
            "cell_tower" -> 2.5f
            "electrical_substation" -> 2.0f
            "water_treatment" -> 1.5f
            "hospital" -> 1.0f
            "school" -> 1.0f
            else -> 0.5f
        }
    }
    
    private fun getServiceTypeDisplayName(serviceType: String): String {
        return when (serviceType.lowercase()) {
            "hospital" -> "Hospital"
            "school" -> "Colegio"
            "police" -> "Policía"
            "fire_station" -> "Bomberos"
            "post_office" -> "Correo"
            "bank" -> "Banco"
            "electrical_substation" -> "Subestación"
            "water_treatment" -> "Planta H2O"
            "water_storage" -> "Tanque H2O"
            "gas_station" -> "Est. Gas"
            "cell_tower" -> "Torre Celular"
            else -> serviceType.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }
    
    fun shouldShowInAR(service: PublicService, userLocation: Location): Boolean {
        val serviceLocation = Location("").apply {
            latitude = service.latitude
            longitude = service.longitude
        }
        
        val distance = userLocation.distanceTo(serviceLocation)
        return distance <= MAX_AR_DISTANCE
    }
}

fun Location.bearingTo(destination: Location): Float {
    val lat1 = Math.toRadians(this.latitude)
    val lat2 = Math.toRadians(destination.latitude)
    val deltaLon = Math.toRadians(destination.longitude - this.longitude)
    
    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
    
    return Math.toDegrees(atan2(y, x)).toFloat()
}
