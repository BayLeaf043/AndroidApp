package com.example.project.data.remote

import com.google.android.gms.tasks.Task

internal fun <T> Task<T>.handle(
    onSuccess: (T) -> Unit,
    onError: (Exception) -> Unit
) {
    addOnSuccessListener { onSuccess(it) }
    addOnFailureListener { onError(it) }
}