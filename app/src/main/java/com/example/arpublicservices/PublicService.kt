package com.example.arpublicservices

data class PublicService(
    val id: String,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)

data class MapBounds(
    val southwest: com.google.android.gms.maps.model.LatLng,
    val northeast: com.google.android.gms.maps.model.LatLng
)
