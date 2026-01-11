package com.example.project.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.project.data.remote.FirebaseProvider
import com.example.project.data.remote.FirestorePaths
import com.example.project.data.remote.handle

class TrainingDetailsRepository(
    private val db: FirebaseFirestore = FirebaseProvider.db,
    private val bookingRepo: BookingRepository = BookingRepository(),
    private val servicesRepo: ServicesRepository = ServicesRepository()
)  {

    data class GroupMeta(
        val ageGroup: String,
        val level: String,
        val trainingType: String
    )

    data class MembershipCandidate(
        val purchaseId: String,
        val message: String
    )

    data class SingleCandidate(
        val serviceId: String,
        val priceText: String
    )

    fun loadGroupMeta(
        groupId: String,
        onResult: (GroupMeta?, String?) -> Unit
    ) {
        db.collection(FirestorePaths.GROUPS).document(groupId).get()
            .handle(
                onSuccess = { doc ->
                    if (!doc.exists()) {
                        onResult(null, "Групу не знайдено")
                        return@handle
                    }
                    onResult(
                        GroupMeta(
                            ageGroup = doc.getString("ageGroup").orEmpty(),
                            level = doc.getString("level").orEmpty(),
                            trainingType = doc.getString("trainingType").orEmpty()
                        ),
                        null
                    )
                },
                onError = { e -> onResult(null, e.localizedMessage) }
            )
    }

    fun checkAlreadyBooked(
        userId: String,
        sessionId: String,
        onResult: (Boolean) -> Unit
    ) {
        bookingRepo.hasActiveBookingForSession(userId, sessionId, onResult)
    }

    /**
     * Пошук активного абонемента, сумісного з групою:
     * - purchase active
     * - ageGroup == groupAge
     * - visitsUsed < visitsTotal
     * - service(trainingType + level) == group(trainingType + level)
     */
    fun findMatchingActiveMembership(
        userId: String,
        groupMeta: GroupMeta,
        onResult: (MembershipCandidate?, String?) -> Unit
    ) {
        db.collection(FirestorePaths.PURCHASES)
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "membership")
            .whereEqualTo("status", "active")
            .whereEqualTo("ageGroup", groupMeta.ageGroup)
            .get()
            .handle(
                onSuccess = { qs ->
                    if (qs.isEmpty) {
                        onResult(null, null)
                        return@handle
                    }

                    val docs = qs.documents

                    fun checkNext(i: Int) {
                        if (i >= docs.size) {
                            onResult(null, null)
                            return
                        }

                        val pDoc = docs[i]
                        val purchaseId = pDoc.id

                        val visitsTotal = (pDoc.getLong("visitsTotal") ?: 0L).toInt()
                        val visitsUsed = (pDoc.getLong("visitsUsed") ?: 0L).toInt()
                        if (visitsTotal > 0 && visitsUsed >= visitsTotal) {
                            checkNext(i + 1)
                            return
                        }

                        val serviceId = pDoc.getString("serviceId").orEmpty()
                        if (serviceId.isBlank()) {
                            checkNext(i + 1)
                            return
                        }

                        db.collection(FirestorePaths.SERVICES).document(serviceId).get()
                            .handle(
                                onSuccess = { sDoc ->
                                    val t = sDoc.getString("trainingType").orEmpty()
                                    val lvl = sDoc.getString("level").orEmpty()

                                    val matches =
                                        t.equals(groupMeta.trainingType, true) &&
                                                lvl.equals(groupMeta.level, true)

                                    if (matches) {
                                        val left = (visitsTotal - visitsUsed).coerceAtLeast(0)
                                        onResult(
                                            MembershipCandidate(
                                                purchaseId = purchaseId,
                                                message = "✅ Можна використати абонемент ($left/$visitsTotal)"
                                            ),
                                            null
                                        )
                                    } else {
                                        checkNext(i + 1)
                                    }
                                },
                                onError = { _ ->
                                    checkNext(i + 1)
                                }
                            )
                    }

                    checkNext(0)
                },
                onError = { e -> onResult(null, e.localizedMessage) }
            )
    }

    /**
     * Single service: беремо активні single послуги і шукаємо по trainingType.
     */
    fun findSingleForTrainingType(
        trainingType: String,
        onResult: (SingleCandidate?, String?) -> Unit
    ) {
        servicesRepo.loadSingleServices { services, err ->
            if (err != null) {
                onResult(null, err)
                return@loadSingleServices
            }
            val list = services.orEmpty()
            val match = list.firstOrNull {
                it.isActive &&
                        it.kind.equals("single", true) &&
                        it.trainingType.equals(trainingType, true)
            }

            if (match == null) {
                onResult(null, "Разову послугу не знайдено")
            } else {
                onResult(
                    SingleCandidate(
                        serviceId = match.id,
                        priceText = "Разове тренування: ${match.price} грн"
                    ),
                    null
                )
            }
        }
    }
}