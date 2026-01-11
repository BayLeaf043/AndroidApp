package com.example.project.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRemoteDataSource(
    private val auth: FirebaseAuth = FirebaseProvider.auth
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signInWithGoogle(
        idToken: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) onSuccess(user)
                else onError(IllegalStateException("FirebaseUser is null"))
            }
            .addOnFailureListener { onError(it) }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteAuthUser(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError(IllegalStateException("No authorized user"))
            return
        }
        user.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}