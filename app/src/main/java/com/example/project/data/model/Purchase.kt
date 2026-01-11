package com.example.project.data.model

data class Purchase(
    val userId: String = "",
    val serviceId: String = "",

    val type: String = "",
    val status: String = "pending",

    val createdAt: Long = 0L,

    val visitsTotal: Int = 0,
    val visitsUsed: Int = 0,

    val ageGroup: String = "18+", // 18+ 10-17 5-9

    val startAt: Long = 0L,
    val endAt: Long = 0L
) {
    val visitsLeft: Int
        get() = (visitsTotal - visitsUsed).coerceAtLeast(0)

}
