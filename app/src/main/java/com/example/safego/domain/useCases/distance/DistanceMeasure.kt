package com.example.safego.domain.useCases.distance

import android.location.Location

object DistanceMeasure {
    fun calculateDistance(
        userLat: Double, userLng: Double,
        placeLat: Double, placeLng: Double
    ): Float {
        val userLocation = Location("").apply {
            latitude = userLat
            longitude = userLng
        }

        val placeLocation = Location("").apply {
            latitude = placeLat
            longitude = placeLng
        }

        val distanceInMeters = userLocation.distanceTo(placeLocation) // بالمتر
        return distanceInMeters
    }

}