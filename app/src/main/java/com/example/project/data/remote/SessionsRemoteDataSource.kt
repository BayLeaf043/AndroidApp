package com.example.project.data.remote

import com.example.project.data.model.Session
import com.google.firebase.firestore.FieldPath
class SessionsRemoteDataSource {
    private val db = FirebaseProvider.db

    fun getSessionsForDay(
        dayStart: Long,
        dayEnd: Long,
        onSuccess: (List<Session>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.SESSIONS)
            .whereGreaterThanOrEqualTo("startAt", dayStart)
            .whereLessThan("startAt", dayEnd)
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(Session::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }

    fun upsertSessions(
        sessions: List<Session>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val chunks = sessions.chunked(450) // < 500 для batch
        commitChunks(chunks, 0, onSuccess, onError)
    }

    private fun commitChunks(
        chunks: List<List<Session>>,
        index: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (index >= chunks.size) {
            onSuccess()
            return
        }

        val batch = db.batch()
        val part = chunks[index]

        for (s in part) {
            if (s.id.isBlank()) continue

            val ref = db.collection(FirestorePaths.SESSIONS).document(s.id)
            batch.set(ref, s)
        }

        batch.commit()
            .addOnSuccessListener { commitChunks(chunks, index + 1, onSuccess, onError) }
            .addOnFailureListener { onError(it) }
    }

    fun hasAnySessionsAhead(
        fromMillis: Long,
        toMillis: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.SESSIONS)
            .whereGreaterThanOrEqualTo("startAt", fromMillis)
            .whereLessThan("startAt", toMillis)
            .limit(1)
            .get()
            .handle(
                onSuccess = { qs -> onSuccess(!qs.isEmpty) },
                onError = onError
            )
    }

    fun getSessionStartAt(
        sessionId: String,
        onSuccess: (Long?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.SESSIONS)
            .document(sessionId)
            .get()
            .handle(
                onSuccess = { doc ->
                    if (!doc.exists()) {
                        onSuccess(null)
                    } else {
                        onSuccess(doc.getLong("startAt"))
                    }
                },
                onError = onError
            )
    }

    fun getSessionsByIds(
        sessionIds: List<String>,
        onSuccess: (List<Session>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ids = sessionIds.distinct().filter { it.isNotBlank() }
        if (ids.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        val chunks = ids.chunked(10) // Firestore whereIn max 10
        val result = mutableListOf<Session>()
        var finished = 0

        for (part in chunks) {
            db.collection(FirestorePaths.SESSIONS)
                .whereIn(FieldPath.documentId(), part)
                .get()
                .handle(
                    onSuccess = { qs ->
                        val list = qs.documents.mapNotNull { d ->
                            d.toObject(Session::class.java)?.copy(id = d.id)
                        }
                        result.addAll(list)
                        finished++
                        if (finished == chunks.size) onSuccess(result)
                    },
                    onError = { e -> onError(e) }
                )
        }
    }
}