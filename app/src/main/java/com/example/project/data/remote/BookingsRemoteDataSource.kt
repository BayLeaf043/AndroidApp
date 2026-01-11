package com.example.project.data.remote

import com.example.project.data.model.Booking
class BookingsRemoteDataSource {
    private val db = FirebaseProvider.db

    fun getBookingsForUser(
        userId: String,
        onSuccess: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.BOOKINGS)
            .whereEqualTo("userId", userId)
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(Booking::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }

    fun hasActiveBookingForSession(
        userId: String,
        sessionId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val bookingId = "${userId}_${sessionId}"

        db.collection(FirestorePaths.BOOKINGS)
            .document(bookingId)
            .get()
            .handle(
                onSuccess = { doc ->
                    val active = doc.exists() && doc.getString("status") == "active"
                    onSuccess(active)
                },
                onError = onError
            )
    }

    fun getActiveBookingsForPurchase(
        userId: String,
        purchaseId: String,
        onSuccess: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.BOOKINGS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("purchaseId", purchaseId)
            .whereEqualTo("status", "active")
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(Booking::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }
}