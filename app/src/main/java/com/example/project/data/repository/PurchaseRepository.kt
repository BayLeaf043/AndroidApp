package com.example.project.data.repository

import com.example.project.data.model.Group
import com.example.project.data.model.Purchase
import com.example.project.data.model.Service
import com.example.project.data.remote.FirebaseProvider
import com.example.project.data.remote.FirestorePaths
import com.example.project.data.remote.handle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PurchaseRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.db,
    private val auth: FirebaseAuth = FirebaseProvider.auth
) {

    fun loadService(serviceId: String, onResult: (Service?, String?) -> Unit) {
        firestore.collection(FirestorePaths.SERVICES)
            .document(serviceId)
            .get()
            .handle(
                onSuccess = { doc ->
                    val svc = doc.toObject(Service::class.java)?.copy(id = doc.id)
                    onResult(svc, null)
                },
                onError = { e ->
                    onResult(null, e.localizedMessage)
                }
            )
    }

    fun loadCompatibleGroups(
        service: Service,
        ageGroup: String,
        onResult: (List<Group>?, String?) -> Unit
    ) {
        firestore.collection(FirestorePaths.GROUPS)
            .whereEqualTo("isActive", true)
            .whereEqualTo("trainingType", service.trainingType)
            .whereEqualTo("level", service.level)
            .whereEqualTo("ageGroup", ageGroup)
            .get()
            .handle(
                onSuccess = { snap ->
                    val groups = snap.documents.mapNotNull { d ->
                        d.toObject(Group::class.java)?.copy(id = d.id)
                    }
                    onResult(groups, null)
                },
                onError = { e ->
                    onResult(null, e.localizedMessage)
                }
            )
    }

    fun createPendingMembership(
        service: Service,
        ageGroup: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onResult(false, "Користувач не авторизований")
            return
        }

        val purchase = Purchase(
            userId = uid,
            serviceId = service.id,
            type = "membership",
            status = "pending",
            createdAt = System.currentTimeMillis(),
            visitsTotal = service.sessionsCount,
            visitsUsed = 0,
            ageGroup = ageGroup,
            startAt = 0L,
            endAt = 0L
        )

        firestore.collection(FirestorePaths.PURCHASES)
            .add(purchase)
            .handle(
                onSuccess = { onResult(true, null) },
                onError = { e -> onResult(false, e.localizedMessage) }
            )
    }

    fun activateMembershipToday(
        purchaseId: String,
        durationDays: Int = 30,
        onResult: (Boolean, String?) -> Unit
    ) {
        val now = System.currentTimeMillis()
        val endAt = now + durationDays * 24L * 60L * 60L * 1000L

        val updates: Map<String, Any> = mapOf(
            "startAt" to now,
            "endAt" to endAt,
            "status" to "active"
        )

        firestore.collection(FirestorePaths.PURCHASES)
            .document(purchaseId)
            .update(updates)
            .handle(
                onSuccess = { onResult(true, null) },
                onError = { e -> onResult(false, e.localizedMessage) }
            )
    }
}