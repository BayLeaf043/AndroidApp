package com.example.project.viewmodel.membership

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.project.data.repository.PurchaseRepository

class PurchaseViewModelFactory: ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
            return PurchaseViewModel(PurchaseRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}