package com.example.project.viewmodel.schedule

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.project.data.repository.TrainingDetailsRepository
import com.example.project.data.repository.BookingRepository

class TrainingDetailsViewModelFactory: ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingDetailsViewModel::class.java)) {
            val repo = TrainingDetailsRepository()
            val bookingRepo = BookingRepository()
            return TrainingDetailsViewModel(repo, bookingRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}