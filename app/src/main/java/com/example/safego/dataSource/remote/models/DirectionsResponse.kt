package com.example.safego.dataSource.remote.models

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline?,
    val legs: List<Leg>
)

data class OverviewPolyline(
    @SerializedName("points")
    val points: String
)

data class Leg(
    val distance: Distance,
    val duration: Duration
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)
