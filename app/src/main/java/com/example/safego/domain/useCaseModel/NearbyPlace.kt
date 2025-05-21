package com.example.safego.domain.useCaseModel

import com.google.android.gms.maps.model.LatLng

data class NearbyPlace(
    val latLng: LatLng,
    val name :String,
    val type :String,
    val distance :String,
    val image :Int,
    val rating :String ="",
    val status :String ="",
    val openingHours :String="",
    val time :String=""
)