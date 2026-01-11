package com.example.project.data.model

data class MyTrainingUi(
    val bookingId: String,
    val sessionId: String,
    val groupId: String,

    val title: String,
    val trainerName: String,

    val startAt: Long,
    val endAt: Long,

    val source: String,      // "single" / "membership"
    val status: String,      // "active" / "canceled"
    val isPassed: Boolean
)
