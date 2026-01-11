package com.example.project.viewmodel.schedule

import com.example.project.data.repository.ScheduleRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.project.data.model.ScheduleUi
import androidx.lifecycle.LiveData
import java.util.Calendar
import com.example.project.data.remote.SessionsRemoteDataSource
import com.example.project.data.repository.ScheduleGeneratorRepository

class ScheduleViewModel (
    private val repository: ScheduleRepository,
    private val sessionsRemote: SessionsRemoteDataSource = SessionsRemoteDataSource(),
    private val generatorRepo: ScheduleGeneratorRepository = ScheduleGeneratorRepository()
) : ViewModel() {

    private val _trainings = MutableLiveData<List<ScheduleUi>>()
    val trainings: LiveData<List<ScheduleUi>> = _trainings

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var autoSeedDone = false

    fun ensureSeeded(daysAhead: Int = 365, reloadDate: Calendar? = null) {
        if (autoSeedDone) return
        autoSeedDone = true

        val now = System.currentTimeMillis()
        val weekAhead = now + 7L * 24 * 60 * 60 * 1000

        sessionsRemote.hasAnySessionsAhead(
            fromMillis = now,
            toMillis = weekAhead,
            onSuccess = { has ->
                if (has) return@hasAnySessionsAhead

                generatorRepo.generateSessionsAhead(daysAhead = daysAhead) { ok, err ->
                    if (!ok) {
                        _error.postValue(err ?: "Не вдалося згенерувати розклад")
                        return@generateSessionsAhead
                    }

                    reloadDate?.let { loadForDate(it) }
                }
            },
            onError = { }
        )
    }

    fun loadForDate(date: Calendar) {
        _loading.value = true
        _error.value = null

        repository.loadTrainingsForDate(date) { list, error ->
            _loading.postValue(false)

            if (error != null) {
                _error.postValue(error)
                _trainings.postValue(emptyList())
                return@loadTrainingsForDate
            }

            _trainings.postValue(list ?: emptyList())
        }
    }
}