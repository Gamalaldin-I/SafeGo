package com.example.safego.domain.useCaseModel

import com.google.android.gms.maps.model.LatLng

data class JourneyDetails(
    var duration: String = "",
    var distance: String = "",
    var vehicleType: String,
    var speed: Float = 0f,
    val start: LatLng = LatLng(0.0, 0.0),
    val end: LatLng = LatLng(0.0, 0.0),
    var startTime :String = "",
    var endTime :String = ""
)