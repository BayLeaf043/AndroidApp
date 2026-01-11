package com.example.project.data.remote

import com.example.project.data.model.Trainer

class TrainersRemoteDataSource {
    private val db = FirebaseProvider.db

    fun getActiveTrainers(
        onSuccess: (List<Trainer>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.TRAINERS)
            .whereEqualTo("isActive", true)
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(Trainer::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }
}