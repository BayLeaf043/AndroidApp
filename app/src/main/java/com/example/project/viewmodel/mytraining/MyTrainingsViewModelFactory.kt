package com.example.project.viewmodel.mytraining

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.project.data.repository.MyTrainingsRepository

class MyTrainingsViewModelFactory: ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyTrainingsViewModel::class.java)) {
            return MyTrainingsViewModel(MyTrainingsRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}