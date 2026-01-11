package com.example.project.data.repository

import com.example.project.data.model.User
import com.google.firebase.auth.FirebaseUser
import com.example.project.data.remote.AuthRemoteDataSource
import com.example.project.data.remote.UsersRemoteDataSource

class AuthRepository(
    private val authRemote: AuthRemoteDataSource = AuthRemoteDataSource(),
    private val usersRemote: UsersRemoteDataSource = UsersRemoteDataSource()
) {

    fun getFirebaseUser(): FirebaseUser? = authRemote.getCurrentUser()

    fun signInWithGoogle(
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        authRemote.signInWithGoogle(
            idToken = idToken,
            onSuccess = { onResult(true, null) },
            onError = { onResult(false, it.localizedMessage) }
        )
    }

    fun loadUserProfile(onResult: (User?) -> Unit) {
        val fbUser = authRemote.getCurrentUser()
        if (fbUser == null) {
            onResult(null)
            return
        }
        usersRemote.getUser(
            uid = fbUser.uid,
            onSuccess = { onResult(it) },
            onError = { onResult(null) }
        )
    }

    fun saveUserProfile(
        phone: String,
        firstName: String,
        lastName: String,
        gender: String,
        birthDate: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val fbUser = authRemote.getCurrentUser()
        if (fbUser == null) {
            onResult(false, "Користувач не авторизований")
            return
        }

        val profile = User(
            uid = fbUser.uid,
            phone = phone,
            email = fbUser.email ?: "",
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            birthDate = birthDate,
            createdAt = System.currentTimeMillis()
        )

        usersRemote.createOrUpdateUser(
            user = profile,
            onSuccess = { onResult(true, null) },
            onError = { onResult(false, it.localizedMessage) }
        )
    }

    fun signOut() = authRemote.signOut()


    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val fbUser = authRemote.getCurrentUser()
        if (fbUser == null) {
            onResult(false, "Немає авторизованого користувача")
            return
        }

        val uid = fbUser.uid

        usersRemoteDelete(
            uid = uid,
            onDone = {
                authRemote.deleteAuthUser(
                    onSuccess = { onResult(true, null) },
                    onError = { onResult(false, it.localizedMessage) }
                )
            },
            onError = {
                authRemote.deleteAuthUser(
                    onSuccess = { onResult(true, null) },
                    onError = { onResult(false, it.localizedMessage) }
                )
            }
        )
    }

    private fun usersRemoteDelete(
        uid: String,
        onDone: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        usersRemote.deleteUser(
            uid = uid,
            onSuccess = onDone,
            onError = onError
        )
    }
}