package com.example.project.data.model

//одне тренування зі списку
data class ScheduleUi(
    val sessionId: String,
    val groupId: String,
    val startAt: Long,
    val endAt: Long,
    val title: String,
    val trainerName: String,
    val capacity: Int,
    val isPassed: Boolean
)