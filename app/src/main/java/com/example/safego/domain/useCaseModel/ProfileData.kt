package com.example.safego.domain.useCaseModel

data class ProfileData(
    val imagePath: String,
    val name: String,
    val government: String,
    val birthDay: String,
    val gender: String,
    val ssn: String,
    val phone: String,
    val email: String,
)
