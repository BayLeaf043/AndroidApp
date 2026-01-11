package com.example.project.viewmodel.membership

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.project.data.repository.MyMembershipRepository
import com.example.project.data.repository.PurchaseRepository

class MyMembershipViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyMembershipViewModel::class.java)) {
            return MyMembershipViewModel(
                MyMembershipRepository(),
                PurchaseRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}