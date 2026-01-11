package com.example.project.data.model

data class Session(
    val id: String = "",
    val groupId: String = "",
    val trainerId: String = "",

    val startAt: Long = 0L,
    val endAt: Long = 0L,

    val capacity: Int = 0,
    val status: String = "",
    val bookedActive: Int = 0
)
