package com.example.safego.dataSource.local.repo
import android.content.Context
import com.example.safego.dataSource.local.model.Destination
import com.example.safego.dataSource.local.room.AppDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalRepoImp(context: Context) : LocalRepo {
    private var db: AppDB = AppDB.getInstance(context)

    override suspend fun insertDestination(destination: Destination) {
            withContext(Dispatchers.IO) {
            db.destinationDao().insertDestination(destination)}
        }

        override suspend fun getAllDestinations(): List<Destination> =
            withContext(Dispatchers.IO) {
                db.destinationDao().getAllDestinations()
            }

        override suspend fun deleteDestination(destinationName:String) {
            withContext(Dispatchers.IO) {
                db.destinationDao().deleteDestinationById(destinationName)
            }
        }

        override suspend fun deleteAllDestinations() {
            withContext(Dispatchers.IO) {
                db.destinationDao().deleteAllDestinations()

            }}

        override suspend fun getDestinationByName(destinationName: String): Destination? =
            withContext(Dispatchers.IO) {
                db.destinationDao().getDestinationByName(destinationName)
            }
    }
