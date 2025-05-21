package com.example.safego.dataSource.local.repo

import com.example.safego.dataSource.local.model.Destination

interface LocalRepo {
    suspend fun insertDestination(destination: Destination)
    suspend fun getAllDestinations(): List<Destination>
    suspend fun deleteDestination(destinationName: String)
    suspend fun deleteAllDestinations()
    suspend fun getDestinationByName(destinationName: String): Destination?
}