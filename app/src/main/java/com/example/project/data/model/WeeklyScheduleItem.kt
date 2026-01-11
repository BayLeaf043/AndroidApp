package com.example.project.data.model

data class WeeklyScheduleItem(
    val id: String = "",
    val dayOfWeek: Int = 1,
    val startTime: String = "00:00",
    val durationMinutes: Int = 60
)