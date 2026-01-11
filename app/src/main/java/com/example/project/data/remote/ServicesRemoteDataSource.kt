package com.example.project.data.remote

import com.example.project.data.model.Service

class ServicesRemoteDataSource {
    private val db = FirebaseProvider.db

    fun getActiveServicesByKind(
        kind: String, // "membership" або "single"
        onSuccess: (List<Service>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.SERVICES)
            .whereEqualTo("isActive", true)
            .whereEqualTo("kind", kind)
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(Service::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }
}