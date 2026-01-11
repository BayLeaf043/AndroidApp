package com.example.project.data.repository

import com.example.project.data.model.MyMembershipUi
import com.example.project.data.model.Purchase
import com.example.project.data.remote.FirebaseProvider
import com.example.project.data.remote.FirestorePaths
import com.example.project.data.remote.handle
import com.google.firebase.firestore.FirebaseFirestore
import com.example.project.data.remote.BookingsRemoteDataSource
import com.example.project.data.remote.SessionsRemoteDataSource
import com.example.project.data.model.Booking

class MyMembershipRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.db,
    private val bookingsRemote: BookingsRemoteDataSource = BookingsRemoteDataSource(),
    private val sessionsRemote: SessionsRemoteDataSource = SessionsRemoteDataSource()
) {

    fun loadMyMemberships(
        uid: String,
        onResult: (List<MyMembershipUi>?, String?) -> Unit
    ) {
        firestore.collection(FirestorePaths.PURCHASES)
            .whereEqualTo("userId", uid)
            .whereEqualTo("type", "membership")
            .whereIn("status", listOf("active", "pending"))
            .get()
            .handle(
                onSuccess = { snap ->
                    if (snap.isEmpty) {
                        onResult(emptyList(), null)
                        return@handle
                    }

                    val result = mutableListOf<MyMembershipUi>()
                    var finished = 0
                    val total = snap.size()

                    for (doc in snap.documents) {
                        val p = doc.toObject(Purchase::class.java)

                        if (p == null) {
                            finished++
                            if (finished == total) onResult(sortMemberships(result), null)
                            continue
                        }

                        firestore.collection(FirestorePaths.SERVICES)
                            .document(p.serviceId)
                            .get()
                            .handle(
                                onSuccess = { serviceDoc ->
                                    val title = serviceDoc.getString("title") ?: ""
                                    val trainingType = serviceDoc.getString("trainingType") ?: ""
                                    val level = serviceDoc.getString("level") ?: ""

                                    result.add(
                                        MyMembershipUi(
                                            purchaseId = doc.id,
                                            serviceId = p.serviceId,
                                            title = title,
                                            trainingType = trainingType,
                                            level = level,
                                            ageGroup = p.ageGroup,
                                            startAtMillis = p.startAt,
                                            endAtMillis = p.endAt,
                                            visitsTotal = p.visitsTotal,
                                            visitsUsed = p.visitsUsed,
                                            status = p.status.ifBlank { "pending" }
                                        )
                                    )

                                    finished++
                                    if (finished == total) onResult(sortMemberships(result), null)
                                },
                                onError = { e ->
                                    onResult(null, e.localizedMessage)
                                }
                            )
                    }
                },
                onError = { e -> onResult(null, e.localizedMessage) }
            )
    }

// архів
    fun loadArchiveMemberships(
        uid: String,
        onResult: (List<MyMembershipUi>?, String?) -> Unit
    ) {
        firestore.collection(FirestorePaths.PURCHASES)
            .whereEqualTo("userId", uid)
            .whereEqualTo("type", "membership")
            .whereEqualTo("status", "finished")
            .get()
            .handle(
                onSuccess = { snap ->
                    if (snap.isEmpty) {
                        onResult(emptyList(), null)
                        return@handle
                    }

                    val result = mutableListOf<MyMembershipUi>()
                    var finished = 0
                    val total = snap.size()

                    for (doc in snap.documents) {
                        val p = doc.toObject(Purchase::class.java)

                        if (p == null) {
                            finished++
                            if (finished == total) onResult(sortMemberships(result), null)
                            continue
                        }

                        firestore.collection(FirestorePaths.SERVICES)
                            .document(p.serviceId)
                            .get()
                            .handle(
                                onSuccess = { serviceDoc ->
                                    val title = serviceDoc.getString("title") ?: ""
                                    val trainingType = serviceDoc.getString("trainingType") ?: ""
                                    val level = serviceDoc.getString("level") ?: ""

                                    result.add(
                                        MyMembershipUi(
                                            purchaseId = doc.id,
                                            serviceId = p.serviceId,
                                            title = title,
                                            trainingType = trainingType,
                                            level = level,
                                            ageGroup = p.ageGroup,
                                            startAtMillis = p.startAt,
                                            endAtMillis = p.endAt,
                                            visitsTotal = p.visitsTotal,
                                            visitsUsed = p.visitsUsed,
                                            status = p.status.ifBlank { "finished" }
                                        )
                                    )

                                    finished++
                                    if (finished == total) onResult(sortArchive(result), null)
                                },
                                onError = { e ->
                                    onResult(null, e.localizedMessage)
                                }
                            )
                    }
                },
                onError = { e -> onResult(null, e.localizedMessage) }
            )
    }

    private fun sortMemberships(list: List<MyMembershipUi>): List<MyMembershipUi> {
        return list.sortedWith(
            compareBy<MyMembershipUi>(
                { if (it.status == "active") 0 else 1 }, // active first
                { if (it.status == "active") it.endAtMillis else it.startAtMillis } // pending by startAt
            )
        )
    }

    private fun sortArchive(list: List<MyMembershipUi>): List<MyMembershipUi> {
        return list.sortedByDescending {
            if (it.endAtMillis > 0L) it.endAtMillis else it.startAtMillis
        }
    }

    fun refreshFinishedMemberships(
        uid: String,
        onDone: (String?) -> Unit // null якщо ок, або error msg
    ) {
        val now = System.currentTimeMillis()

        firestore.collection(FirestorePaths.PURCHASES)
            .whereEqualTo("userId", uid)
            .whereEqualTo("type", "membership")
            .whereEqualTo("status", "active")
            .get()
            .handle(
                onSuccess = { qs ->
                    if (qs.isEmpty) {
                        onDone(null)
                        return@handle
                    }

                    val docs = qs.documents
                    var index = 0

                    fun next() {
                        if (index >= docs.size) {
                            onDone(null)
                            return
                        }

                        val doc = docs[index++]
                        val purchaseId = doc.id

                        val endAt = doc.getLong("endAt") ?: 0L
                        val visitsTotal = (doc.getLong("visitsTotal") ?: 0L).toInt()
                        val visitsUsed = (doc.getLong("visitsUsed") ?: 0L).toInt()

                        // 1) якщо термін дії минув -> finished одразу
                        if (endAt > 0L && now > endAt) {
                            markFinished(purchaseId) { _ ->
                                next()
                            }
                            return
                        }

                        // 2) якщо ліміт НЕ вичерпано -> точно не finished
                        if (visitsTotal > 0 && visitsUsed < visitsTotal) {
                            next()
                            return
                        }

                        // 3) visitsUsed>=visitsTotal -> finished тільки якщо НЕМАЄ майбутніх booking цього purchase
                        bookingsRemote.getActiveBookingsForPurchase(
                            userId = uid,
                            purchaseId = purchaseId,
                            onSuccess = { bookings ->
                                if (bookings.isEmpty()) {
                                    // немає активних бронювань -> можемо завершити
                                    markFinished(purchaseId) { _ -> next() }
                                    return@getActiveBookingsForPurchase
                                }

                                // перевіряємо, чи є хоча б одне майбутнє тренування серед booking
                                hasAnyFutureSession(bookings, now) { hasFuture ->
                                    if (hasFuture) {
                                        // є майбутнє -> НЕ завершуємо
                                        next()
                                    } else {
                                        // всі вже минулі -> завершуємо
                                        markFinished(purchaseId) { _ -> next() }
                                    }
                                }
                            },
                            onError = {
                                next()
                            }
                        )
                    }

                    next()
                },
                onError = { e ->
                    onDone(e.localizedMessage ?: "Помилка перевірки абонементів")
                }
            )
    }


     //Перевіряє чи є у purchaseId бронювання на МАЙБУТНІ сесії.
    private fun hasAnyFutureSession(
        bookings: List<Booking>,
        now: Long,
        onResult: (Boolean) -> Unit
    ) {
        var i = 0

        fun step() {
            if (i >= bookings.size) {
                onResult(false)
                return
            }

            val b = bookings[i++]
            val sessionId = b.sessionId
            if (sessionId.isBlank()) {
                step()
                return
            }

            sessionsRemote.getSessionStartAt(
                sessionId = sessionId,
                onSuccess = { startAt ->
                    if (startAt != null && startAt >= now) {
                        onResult(true)
                    } else {
                        step()
                    }
                },
                onError = {
                    step()
                }
            )
        }

        step()
    }

    private fun markFinished(
        purchaseId: String,
        onDone: (Boolean) -> Unit
    ) {
        firestore.collection(FirestorePaths.PURCHASES)
            .document(purchaseId)
            .update("status", "finished")
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

}