package com.example.safego.domain.useCases.googleMapFeatures

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.safego.R
import com.example.safego.dataSource.local.AppInfo
import com.example.safego.dataSource.remote.RetrofitBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.contracts.contract

class MapDirector {
    //vars
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private var currentAzimuth : Float = 0f


    //fun

    //search And Go To Location Zooming
    fun searchAndGoToLocation(locationName: String, context: Context, mMap: GoogleMap) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocationName(locationName, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)

                mMap.clear()
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
            } else {
                Toast.makeText(context, "Non existent Location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error while searching", Toast.LENGTH_SHORT).show()

        }
    }
    //Mark The Current Location
    fun markLastLocationOnMap(context: Context,contextA:FragmentActivity,myMarker: Marker?, mMap: GoogleMap, icon: Bitmap?){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                contextA,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val myLatLng = LatLng(it.latitude, it.longitude)
                var marker = myMarker
                if (marker == null) {


                    if (icon != null) {
                        val smallMarker = Bitmap.createScaledBitmap(icon, 100, 100, false)

                        marker = mMap.addMarker(
                            MarkerOptions()
                                .position(myLatLng)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .anchor(0.5f, 0.5f)
                        )
                    } else {
                        Log.e("MapActivity", "Image resource is null!")
                    }
                } else {
                    marker.position = myLatLng
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18f))
            }
        }
    }
    //orientation Sensor
    fun startOrientationListener(sensorManager: SensorManager, myMarker: Marker?) {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

                    val smoothAzimuth = (currentAzimuth + (azimuth - currentAzimuth) * 0.1f)
                    currentAzimuth = smoothAzimuth
                    myMarker?.rotation = smoothAzimuth
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(rotationListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
    }

    //fun draw the route and getInfo
    suspend fun getRouteAndDurationAndDistance(origin: LatLng, destination: LatLng,context: Context):Triple<String,String,String> {
            try {
                val response = RetrofitBuilder.instance.getDirections(
                    origin = "${origin.latitude},${origin.longitude}",
                    destination = "${destination.latitude},${destination.longitude}",
                    apiKey = AppInfo.googleMapKay
                )

                if (response.status != "OK" || response.routes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        showToast("Failed to get directions: ${response.status}",context)
                    }
                    return Triple("0","0","")
                }

                val polyline = response.routes[0].overviewPolyline?.points ?: ""
                val leg = response.routes.firstOrNull()?.legs?.firstOrNull()
                val distance = leg?.distance?.text ?: "unknown"
                val duration = leg?.duration?.text ?: "unknown"

                if (polyline.isBlank()){
                    withContext(Dispatchers.Main) {
                        showToast("The polyline is blank.",context)
                    }
                    return Triple("0","0","")
                }
                return Triple(duration,distance,polyline)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error while getting directions: ${e.localizedMessage}",context)
                }
                return Triple("0","0","")
            }
        }

    fun drawPolyline(polyline: String, map: GoogleMap) {
        val decodedPath = PolyUtil.decode(polyline)
         val startColor = Color.parseColor("#079df8")
            val endColor = Color.parseColor("#f948ee")

            for (i in 0 until decodedPath.size - 1) {
                val fraction = i.toFloat() / decodedPath.size
                val interpolatedColor = interpolateColor(startColor, endColor, fraction)

                map.addPolyline(
                    PolylineOptions()
                        .add(decodedPath[i], decodedPath[i + 1])
                        .width(12f)
                        .color(interpolatedColor)
                        .geodesic(true)
                )
            }
            val boundsBuilder = LatLngBounds.builder()
            decodedPath.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }

    private fun showToast(message: String,context: Context) {
    Toast.makeText(context , message, Toast.LENGTH_LONG).show()
    }

    private fun interpolateColor(colorStart: Int, colorEnd: Int, ratio: Float): Int {
    val inverseRatio = 1 - ratio
    val a = (Color.alpha(colorStart) * inverseRatio + Color.alpha(colorEnd) * ratio).toInt()
    val r = (Color.red(colorStart) * inverseRatio + Color.red(colorEnd) * ratio).toInt()
    val g = (Color.green(colorStart) * inverseRatio + Color.green(colorEnd) * ratio).toInt()
    val b = (Color.blue(colorStart) * inverseRatio + Color.blue(colorEnd) * ratio).toInt()
    return Color.argb(a, r, g, b)
    }
}
