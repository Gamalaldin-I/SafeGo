package com.example.safego.dataSource.local.room
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.safego.dataSource.local.dao.DestinationDao
import com.example.safego.dataSource.local.model.Destination
@Database(entities = [Destination::class], version = 1, exportSchema = false)
abstract class AppDB : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getInstance(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "safe_go_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
