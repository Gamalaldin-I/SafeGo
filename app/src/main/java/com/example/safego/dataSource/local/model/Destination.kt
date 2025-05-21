package com.example.safego.dataSource.local.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val latitude: Double,
    val longitude: Double
)
