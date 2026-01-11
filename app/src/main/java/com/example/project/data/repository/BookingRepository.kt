package com.example.project.data.repository

import com.example.project.data.model.Booking
import com.example.project.data.remote.BookingsRemoteDataSource
import com.example.project.data.remote.FirestorePaths
import com.example.project.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
class BookingRepository(
    private val remote: BookingsRemoteDataSource = BookingsRemoteDataSource(),
    private val db: FirebaseFirestore = FirebaseProvider.db
) {

    fun hasActiveBookingForSession(
        userId: String,
        sessionId: String,
        onResult: (Boolean) -> Unit
    ) {
        remote.hasActiveBookingForSession(
            userId = userId,
            sessionId = sessionId,
            onSuccess = { onResult(it) },
            onError = { onResult(false) } // не валимо UI
        )
    }

    fun createSingleBooking(
        userId: String,
        groupId: String,
        sessionId: String,
        serviceId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val bookingId = "${userId}_${sessionId}"

        val bookingRef = db.collection(FirestorePaths.BOOKINGS).document(bookingId)
        val sessionRef = db.collection(FirestorePaths.SESSIONS).document(sessionId)

        val now = System.currentTimeMillis()

        db.runTransaction { tx ->
            // 1) already booked?
            val existing = tx.get(bookingRef)
            if (existing.exists() && existing.getString("status") == "active") {
                throw IllegalStateException("☑️ Ви вже записані на це тренування")
            }

            // 2) session exists?
            val sSnap = tx.get(sessionRef)
            if (!sSnap.exists()) throw IllegalStateException("Тренування не знайдено")

            val startAt = sSnap.getLong("startAt") ?: 0L
            if (startAt == 0L) throw IllegalStateException("Некоректний час тренування")
            if (startAt < now) throw IllegalStateException("Це тренування вже минуло")

            // 3) capacity check
            val capacity = (sSnap.getLong("capacity") ?: 0L).toInt()
            val bookedActive = (sSnap.getLong("bookedActive") ?: 0L).toInt()

            if (capacity > 0 && bookedActive >= capacity) {
                throw IllegalStateException("😔 Місць більше немає")
            }

            // ✅ reserve seat
            if (capacity > 0) {
                tx.update(sessionRef, "bookedActive", bookedActive + 1)
            }

            // 4) create booking
            val booking = Booking(
                id = bookingId,
                userId = userId,
                groupId = groupId,
                sessionId = sessionId,
                purchaseId = "",
                serviceId = serviceId,
                source = "single",
                status = "active",
                createdAt = now
            )
            tx.set(bookingRef, booking)

        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.localizedMessage ?: "Помилка створення запису")
        }
    }

    /**
     * ТРАНЗАКЦІЯ: membership booking + visitsUsed++
     * - абонемент active
     * - не прострочений endAt
     * - ліміт занять
     * - session існує і не минула
     * - session.startAt <= purchase.endAt (якщо endAt заданий)
     */
    fun createMembershipBookingTx(
        userId: String,
        sessionId: String,
        groupId: String,
        purchaseId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val bookingId = "${userId}_${sessionId}"

        val bookingRef = db.collection(FirestorePaths.BOOKINGS).document(bookingId)
        val purchaseRef = db.collection(FirestorePaths.PURCHASES).document(purchaseId)
        val sessionRef = db.collection(FirestorePaths.SESSIONS).document(sessionId)


        val now = System.currentTimeMillis()

        db.runTransaction { tx ->
            // 1) already booked?
            val existing = tx.get(bookingRef)
            if (existing.exists() && existing.getString("status") == "active") {
                throw IllegalStateException("Ви вже записані на це тренування")
            }

            // 2) purchase checks
            val pSnap = tx.get(purchaseRef)
            if (!pSnap.exists()) throw IllegalStateException("Абонемент не знайдено")

            val pStatus = pSnap.getString("status") ?: "pending"
            if (!pStatus.equals("active", true)) {
                throw IllegalStateException("Абонемент не активний")
            }

            val endAt = pSnap.getLong("endAt") ?: 0L
            if (endAt > 0L && now > endAt) {
                throw IllegalStateException("Термін дії абонемента минув")
            }

            val visitsTotal = (pSnap.getLong("visitsTotal") ?: 0L).toInt()
            val visitsUsed = (pSnap.getLong("visitsUsed") ?: 0L).toInt()
            if (visitsTotal > 0 && visitsUsed >= visitsTotal) {
                throw IllegalStateException("Ліміт занять вичерпано")
            }

            val serviceId = pSnap.getString("serviceId").orEmpty()

            // 3) session checks
            val sSnap = tx.get(sessionRef)
            if (!sSnap.exists()) throw IllegalStateException("Тренування не знайдено")

            val sessionStartAt = sSnap.getLong("startAt") ?: 0L
            if (sessionStartAt == 0L) throw IllegalStateException("Некоректний час тренування")

            if (sessionStartAt < now) {
                throw IllegalStateException("Це тренування вже минуло")
            }

            if (endAt > 0L && sessionStartAt > endAt) {
                throw IllegalStateException("Тренування після завершення абонемента")
            }

            val capacity = (sSnap.getLong("capacity") ?: 0L).toInt()
            val bookedActive = (sSnap.getLong("bookedActive") ?: 0L).toInt()

            if (capacity > 0 && bookedActive >= capacity) {
                throw IllegalStateException("😔 Місць більше немає")
            }

            // ✅ резервуємо місце
            if (capacity > 0) {
                tx.update(sessionRef, "bookedActive", bookedActive + 1)
            }

            // 4) списуємо
            tx.update(purchaseRef, "visitsUsed", visitsUsed + 1)

            // 5) створюємо booking
            val booking = Booking(
                id = bookingId,
                userId = userId,
                groupId = groupId,
                sessionId = sessionId,
                purchaseId = purchaseId,
                serviceId = serviceId,
                source = "membership",
                status = "active",
                createdAt = now
            )
            tx.set(bookingRef, booking)
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.localizedMessage ?: "Помилка запису")
        }
    }

}