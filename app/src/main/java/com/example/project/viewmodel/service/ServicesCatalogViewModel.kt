package com.example.project.viewmodel.service

import com.example.project.data.repository.ServicesRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.project.data.model.Service
import androidx.lifecycle.LiveData
import com.example.project.ui.common.Event

class ServicesCatalogViewModel(
    private val repository: ServicesRepository
) : ViewModel() {

    private var allMemberships: List<Service> = emptyList()

    private val _memberships = MutableLiveData<List<Service>>(emptyList())
    val memberships: LiveData<List<Service>> = _memberships

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<Event<String>?>()
    val errorMessage: LiveData<Event<String>?> = _errorMessage

    private var currentQuery: String = ""
    private var selectedLevel: String? = null // null / "basic" / "pro"

    fun loadMemberships() {
        _loading.value = true
        _errorMessage.value = null

        repository.loadMembershipServices { list, error ->
            _loading.postValue(false)

            if (error != null) {
                _errorMessage.postValue(Event(error))
                return@loadMembershipServices
            }

            allMemberships = list ?: emptyList()
            applyFilters()
        }
    }

    fun setSearchQuery(query: String?) {
        currentQuery = query?.trim()?.lowercase().orEmpty()
        applyFilters()
    }

    fun setLevelFilter(level: String?) {
        selectedLevel = level?.trim()?.lowercase() // null / "basic" / "pro"
        applyFilters()
    }

    private fun applyFilters() {
        var list = allMemberships

        // 1) Level filter
        selectedLevel?.let { lvl ->
            list = list.filter { it.level.equals(lvl, ignoreCase = true) }
        }

        // 2) Search filter
        if (currentQuery.isNotEmpty()) {
            val q = currentQuery
            list = list.filter { service ->
                service.title.lowercase().contains(q) ||
                        service.level.lowercase().contains(q) ||
                        service.trainingType.lowercase().contains(q)
            }
        }

        _memberships.value = list
    }
}