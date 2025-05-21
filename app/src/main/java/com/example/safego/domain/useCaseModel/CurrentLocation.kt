package com.example.safego.domain.useCaseModel

data class CurrentLocation(
    var latitude: Double,
    var longitude: Double,
    var address: String,
    var speed: Float=0f,
)