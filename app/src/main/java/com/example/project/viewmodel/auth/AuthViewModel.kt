package com.example.project.viewmodel.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.data.repository.AuthRepository
import com.example.project.data.model.User
import com.example.project.ui.common.Event
import androidx.lifecycle.LiveData

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _currentProfile = MutableLiveData<User?>()
    val currentProfile: LiveData<User?> = _currentProfile

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<Event<String>?>()
    val errorMessage: LiveData<Event<String>?> = _errorMessage

    private val _openUserInfo = MutableLiveData<Event<Unit>?>()
    val openUserInfo: LiveData<Event<Unit>?> = _openUserInfo

    private val _openHome = MutableLiveData<Event<Unit>?>()
    val openHome: LiveData<Event<Unit>?> = _openHome


    fun checkCurrentUser() {
        val user = repository.getFirebaseUser()
        if (user == null) return

        _loading.value = true
        repository.loadUserProfile { profile ->
            _loading.postValue(false)
            _currentProfile.postValue(profile)

            if (profile == null) {
                _openUserInfo.postValue(Event(Unit))
            } else {
                _openHome.postValue(Event(Unit))
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _loading.value = true
        _errorMessage.value = null

        repository.signInWithGoogle(idToken) { success, error ->
            if (!success) {
                _loading.postValue(false)
                _errorMessage.postValue(Event(error ?: "Помилка входу через Google"))
                return@signInWithGoogle
            }

            repository.loadUserProfile { profile ->
                _loading.postValue(false)
                _currentProfile.postValue(profile)

                if (profile == null) _openUserInfo.postValue(Event(Unit))
                else _openHome.postValue(Event(Unit))
            }
        }
    }

    fun saveProfile(
        phone: String,
        firstName: String,
        lastName: String,
        gender: String,
        birthDate: String
    ) {
        _loading.value = true
        _errorMessage.value = null

        repository.saveUserProfile(
            phone = phone,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            birthDate = birthDate
        ) { success, error ->
            if (!success) {
                _loading.postValue(false)
                _errorMessage.postValue(Event(error ?: "Помилка збереження профілю"))
                return@saveUserProfile
            }

            repository.loadUserProfile { profile ->
                _loading.postValue(false)
                _currentProfile.postValue(profile)
                _openHome.postValue(Event(Unit))
            }
        }
    }

    fun getCurrentUserEmail(): String? = repository.getFirebaseUser()?.email

    fun logout() {
        repository.signOut()
        _currentProfile.value = null
        _loading.value = false
        _errorMessage.value = null
    }
}