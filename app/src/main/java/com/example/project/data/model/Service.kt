package com.example.project.data.model

data class Service(
    val id: String = "",
    val title: String = "",
    val price: Int = 0,

    val kind: String = "",
    val trainingType: String = "",
    val level: String = "",

    val sessionsCount: Int = 0,
    val isActive: Boolean = true
)