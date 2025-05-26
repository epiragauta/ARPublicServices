package com.example.arpublicservices

import android.location.Location

class ServiceFilterManager {
    
    enum class ServiceType(val displayName: String, val category: String) {
        HOSPITAL("Hospitales", "health"),
        SCHOOL("Colegios", "education"),
        POLICE("Policía", "security"),
        FIRE_STATION("Bomberos", "emergency"),
        POST_OFFICE("Correos", "postal"),
        BANK("Bancos", "financial"),
        ELECTRICAL_SUBSTATION("Subestaciones Eléctricas", "utilities"),
        WATER_TREATMENT("Plantas de Tratamiento", "utilities"),
        GAS_STATION("Estaciones de Gas", "utilities"),
        CELL_TOWER("Torres de Comunicación", "telecommunications")
    }
    
    fun filterServicesByType(services: List<PublicService>, types: Set<ServiceType>): List<PublicService> {
        if (types.isEmpty()) return services
        
        val typeStrings = types.map { it.name.lowercase() }
        return services.filter { service ->
            typeStrings.any { type ->
                service.type.contains(type, ignoreCase = true)
            }
        }
    }
    
    fun filterServicesByDistance(
        services: List<PublicService>, 
        userLocation: Location, 
        maxDistanceMeters: Float
    ): List<PublicService> {
        return services.filter { service ->
            val serviceLocation = Location("").apply {
                latitude = service.latitude
                longitude = service.longitude
            }
            userLocation.distanceTo(serviceLocation) <= maxDistanceMeters
        }
    }
    
    fun sortServicesByDistance(
        services: List<PublicService>, 
        userLocation: Location
    ): List<PublicService> {
        return services.sortedBy { service ->
            val serviceLocation = Location("").apply {
                latitude = service.latitude
                longitude = service.longitude
            }
            userLocation.distanceTo(serviceLocation)
        }
    }
}
