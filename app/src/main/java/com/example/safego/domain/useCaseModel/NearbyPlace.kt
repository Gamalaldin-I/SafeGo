package com.example.safego.domain.useCaseModel

data class NearbyPlace(
    val name :String,
    val type :String,
    val distance :String,
    val image :Int,
    val rating :String ="",
    val status :String ="",
    val openingHours :String="",
    val time :String=""
)