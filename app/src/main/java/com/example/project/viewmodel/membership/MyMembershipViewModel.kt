package com.example.project.viewmodel.membership

import com.example.project.data.repository.MyMembershipRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.project.data.model.MyMembershipUi
import androidx.lifecycle.LiveData
import com.example.project.data.repository.PurchaseRepository

class MyMembershipViewModel(
    private val repository: MyMembershipRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<MyMembershipUi>>(emptyList())
    val items: LiveData<List<MyMembershipUi>> = _items

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    val activatingId = MutableLiveData<String?>(null)
    val activationSuccess = MutableLiveData(false)

    fun load(uid: String) {
        loading.value = true
        error.value = null
        activationSuccess.value = false

        repository.refreshFinishedMemberships(uid) { errMsg ->
            if (!errMsg.isNullOrBlank()) {
                error.postValue(errMsg)
            }

            repository.loadMyMemberships(uid) { list, err ->
                loading.postValue(false)
                error.postValue(err)
                _items.postValue(list ?: emptyList())
            }
        }
    }

    fun loadArchive(uid: String) {
        loading.value = true
        error.value = null

        repository.loadArchiveMemberships(uid) { list, err ->
            loading.postValue(false)
            error.postValue(err)
            _items.postValue(list ?: emptyList())
        }
    }

    fun activateToday(
        purchaseId: String,
        durationDays: Int = 30,
        uidToReload: String
    ) {
        activatingId.value = purchaseId
        error.value = null
        activationSuccess.value = false

        purchaseRepository.activateMembershipToday(purchaseId, durationDays) { ok, err ->
            activatingId.postValue(null)

            if (ok) {
                activationSuccess.postValue(true)
                load(uidToReload)
            } else {
                error.postValue(err ?: "Не вдалося активувати")
            }
        }
    }

}