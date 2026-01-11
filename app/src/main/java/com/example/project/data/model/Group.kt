package com.example.project.data.model

data class Group(
    val id: String = "",
    val title: String = "",
    val description: String = "",

    val trainingType: String = "",
    val level: String = "",
    val ageGroup: String = "",

    val isActive: Boolean = true,
    val trainerId: String = "",
    val maxStudents: Int = 10
)