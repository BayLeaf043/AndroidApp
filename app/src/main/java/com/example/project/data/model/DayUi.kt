package com.example.project.data.model

import java.util.Calendar

//один день у стрічці
data class DayUi(
    val calendar: Calendar,
    val dayNameShort: String,
    val dayNumber: Int
)