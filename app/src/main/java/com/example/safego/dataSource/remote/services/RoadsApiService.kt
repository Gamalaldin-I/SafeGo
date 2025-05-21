package com.example.safego.dataSource.remote.services

import com.example.safego.dataSource.remote.models.SpeedLimitResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RoadsApiService {
    @GET("v1/speedLimits")
    suspend fun getSpeedLimit(
        @Query("path") path: String,
        @Query("key") apiKey: String
    ): SpeedLimitResponse
}
