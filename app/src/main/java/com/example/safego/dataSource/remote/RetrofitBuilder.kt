package com.example.safego.dataSource.remote

import com.example.safego.dataSource.remote.services.GoogleMapsApiService
import com.example.safego.dataSource.remote.services.RoadsApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    private const val DIRECTIONS_BASE_URL = "https://maps.googleapis.com/maps/api/"
    private const val ROAD_BASE_URL = "https://roads.googleapis.com/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private fun <T> createService(baseUrl: String, serviceClass: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }

    val directionsInstance: GoogleMapsApiService by lazy {
        createService(DIRECTIONS_BASE_URL, GoogleMapsApiService::class.java)
    }

    val roadInstance: RoadsApiService by lazy {
        createService(ROAD_BASE_URL, RoadsApiService::class.java)
    }
}
