package com.example.project.viewmodel.membership

import com.example.project.data.repository.PurchaseRepository
import androidx.lifecycle.ViewModel
import com.example.project.data.model.Group
import com.example.project.data.model.Service
import androidx.lifecycle.MutableLiveData

class PurchaseViewModel (private val repository: PurchaseRepository
) : ViewModel() {

    val service = MutableLiveData<Service?>()

    val selectedAgeGroup = MutableLiveData("18+")
    val compatibleGroups = MutableLiveData<List<Group>>(emptyList())

    // UI text
    val compatibleGroupsText = MutableLiveData("Доступні групи: -")

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    val purchaseSuccess = MutableLiveData(false)

    fun load(serviceId: String) {
        loading.value = true
        error.value = null

        repository.loadService(serviceId) { srv, err ->
            if (err != null || srv == null) {
                loading.postValue(false)
                error.postValue(err ?: "Не знайдено абонемент")
                return@loadService
            }

            service.postValue(srv)

            // load groups for default ageGroup
            reloadCompatibleGroups()
            loading.postValue(false)
        }
    }

    fun setAgeGroup(ageGroup: String) {
        selectedAgeGroup.value = ageGroup
        reloadCompatibleGroups()
    }

    private fun reloadCompatibleGroups() {
        val srv = service.value ?: return
        val age = selectedAgeGroup.value ?: "18+"

        repository.loadCompatibleGroups(srv, age) { list, err ->
            if (err != null) {
                error.postValue(err)
                compatibleGroups.postValue(emptyList())
                compatibleGroupsText.postValue("Доступні групи: -")
                return@loadCompatibleGroups
            }

            val gs = list ?: emptyList()
            compatibleGroups.postValue(gs)

            compatibleGroupsText.postValue(
                if (gs.isEmpty()) "Доступні групи: (немає)"
                else "Доступні групи: " + gs.joinToString { it.description.ifBlank { it.id } }
            )
        }
    }

    fun buy() {
        val srv = service.value ?: return
        val age = selectedAgeGroup.value ?: "18+"

        loading.value = true
        purchaseSuccess.value = false
        error.value = null

        repository.createPendingMembership(srv, age) { ok, err ->
            loading.postValue(false)
            if (ok) purchaseSuccess.postValue(true)
            else error.postValue(err ?: "Помилка оформлення")
        }
    }
}