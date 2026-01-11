package com.example.project.data.repository

import com.example.project.data.model.Service
import com.example.project.data.remote.ServicesRemoteDataSource

class ServicesRepository(
    private val remote: ServicesRemoteDataSource = ServicesRemoteDataSource()
) {

    fun loadMembershipServices(
        onResult: (List<Service>?, String?) -> Unit
    ) {
        remote.getActiveServicesByKind(
            kind = "membership",
            onSuccess = { list -> onResult(list, null) },
            onError = { e -> onResult(null, e.localizedMessage ?: "Помилка завантаження послуг") }
        )
    }

    fun loadSingleServices(
        onResult: (List<Service>?, String?) -> Unit
    ) {
        remote.getActiveServicesByKind(
            kind = "single",
            onSuccess = { list -> onResult(list, null) },
            onError = { e -> onResult(null, e.localizedMessage ?: "Помилка завантаження послуг") }
        )
    }

}