package com.example.safego.dataSource.local.model

data class User(
    val imagePath: String,
    val name: String,
    val ssn: String,
    val phone: String,
    val email: String,
    val password: String,
    val login: Boolean
)
