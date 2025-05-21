package com.example.safego.ui.journey

import com.example.safego.domain.useCaseModel.NearbyPlace

interface NearbyClickListener {
    fun onClick(nearbyPlace: NearbyPlace)
}