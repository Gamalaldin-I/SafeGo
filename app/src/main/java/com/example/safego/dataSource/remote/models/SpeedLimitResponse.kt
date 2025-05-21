package com.example.safego.dataSource.remote.models

data class SpeedLimitResponse(
    val speedLimits: List<SpeedLimit>,
    val snappedPoints: List<SnappedPoint>
)

data class SpeedLimit(
    val placeId: String,
    val speedLimit: Double, // km/h
    val units: String
)

data class SnappedPoint(
    val location: LatLng,
    val originalIndex: Int,
    val placeId: String
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)
