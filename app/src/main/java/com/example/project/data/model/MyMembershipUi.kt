package com.example.project.data.model

data class MyMembershipUi(
    val purchaseId: String,
    val serviceId: String,

    val title: String,           // from service
    val trainingType: String,    // from service
    val level: String,           // from service
    val ageGroup: String,        // from purchase

    val startAtMillis: Long,
    val endAtMillis: Long,

    val visitsTotal: Int,
    val visitsUsed: Int,
    val status: String
) {
    val visitsLeft: Int get() = (visitsTotal - visitsUsed).coerceAtLeast(0)
}