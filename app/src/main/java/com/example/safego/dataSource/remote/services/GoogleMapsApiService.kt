package com.example.safego.dataSource.remote.services

import com.example.safego.dataSource.remote.models.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface GoogleMapsApiService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("mode") mode: String = "driving"
    ): DirectionsResponse
}