package com.example.project.data.model

data class Booking(
    val id: String = "",
    val userId: String = "",
    val groupId: String = "",
    val sessionId: String = "",

    val purchaseId: String = "",
    val serviceId: String = "",

    val source: String = "",
    val status: String = "active",

    val createdAt: Long = 0L
)
