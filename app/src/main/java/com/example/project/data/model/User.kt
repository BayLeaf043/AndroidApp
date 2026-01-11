package com.example.project.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val phone: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
{
    val fullName: String
        get() = (firstName + " " + lastName).trim()
}