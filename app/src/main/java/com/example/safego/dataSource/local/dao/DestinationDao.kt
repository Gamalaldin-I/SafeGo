package com.example.safego.dataSource.local.dao

import com.example.safego.dataSource.local.model.Destination
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface DestinationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: Destination)
    @Query("SELECT * FROM destinations")
    suspend fun getAllDestinations(): List<Destination>

    @Query("DELETE FROM destinations WHERE name = :destinationName")
    suspend fun deleteDestinationById(destinationName: String)

    @Query("DELETE FROM destinations")
    suspend fun deleteAllDestinations()

    @Query("SELECT * FROM destinations WHERE name = :destinationName")
    suspend fun getDestinationByName(destinationName: String): Destination?
}
