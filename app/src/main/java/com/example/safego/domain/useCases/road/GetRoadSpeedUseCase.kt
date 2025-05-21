package com.example.safego.domain.useCases.road

import com.example.safego.dataSource.local.AppInfo
import com.example.safego.dataSource.remote.RetrofitBuilder

class GetRoadSpeedUseCase() {
    suspend fun getTheLimitSpeedOfRoad(lat: Double, lng: Double):Double {
        /*this fun get the speed limit of the road
        * this return the speed of the road in double represent the speed in KM/H
        * if the road is not found return 0.0
        * if has error return 0.0*/
         val path = "$lat,$lng"
        try {
            val response = RetrofitBuilder.roadInstance.getSpeedLimit(path, AppInfo.googleMapKay)
            val speedLimit = response.speedLimits.firstOrNull()?.speedLimit
            // in KMH
            return speedLimit ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
    }
}