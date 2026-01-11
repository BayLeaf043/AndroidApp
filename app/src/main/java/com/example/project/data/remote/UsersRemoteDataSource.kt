package com.example.project.data.remote

import com.google.firebase.firestore.SetOptions
import com.example.project.data.model.User

class UsersRemoteDataSource {
    private val db = FirebaseProvider.db

    fun getUser(
        uid: String,
        onSuccess: (User?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .get()
            .handle(
                onSuccess = { snap ->
                    if (!snap.exists()) onSuccess(null)
                    else onSuccess(snap.toObject(User::class.java)?.copy(uid = snap.id))
                },
                onError = onError
            )
    }

    fun createOrUpdateUser(
        user: User,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (user.uid.isBlank()) {
            onError(IllegalArgumentException("User.uid is blank"))
            return
        }

        db.collection(FirestorePaths.USERS)
            .document(user.uid)
            .set(user, SetOptions.merge())
            .handle(
                onSuccess = { onSuccess() },
                onError = onError
            )
    }

    fun deleteUser(
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .delete()
            .handle(
                onSuccess = { onSuccess() },
                onError = onError
            )
    }
}