package com.example.project.viewmodel.mytraining

import com.example.project.data.repository.MyTrainingsRepository
import androidx.lifecycle.ViewModel
import java.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.project.data.model.MyTrainingUi

class MyTrainingsViewModel(
    private val repo: MyTrainingsRepository
) : ViewModel()  {

    private val _monthCal = MutableLiveData(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) })
    val monthCal: LiveData<Calendar> = _monthCal

    private val _monthTitle = MutableLiveData("")
    val monthTitle: LiveData<String> = _monthTitle

    private val _items = MutableLiveData<List<MyTrainingUi>>(emptyList())
    val items: LiveData<List<MyTrainingUi>> = _items

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    fun init() {
        updateMonthTitle()
    }

    fun prevMonth() {
        val c = (_monthCal.value ?: Calendar.getInstance()).clone() as Calendar
        c.add(Calendar.MONTH, -1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        _monthCal.value = c
        updateMonthTitle()
    }

    fun nextMonth() {
        val c = (_monthCal.value ?: Calendar.getInstance()).clone() as Calendar
        c.add(Calendar.MONTH, 1)
        c.set(Calendar.DAY_OF_MONTH, 1)
        _monthCal.value = c
        updateMonthTitle()
    }

    fun load(uid: String) {
        val cal = _monthCal.value ?: Calendar.getInstance()

        loading.value = true
        error.value = null

        repo.loadMyTrainingsForMonth(uid, cal) { list, err ->
            loading.postValue(false)
            error.postValue(err)
            _items.postValue(list ?: emptyList())
        }
    }

    private fun updateMonthTitle() {
        val cal = _monthCal.value ?: Calendar.getInstance()
        val monthName = when (cal.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "Січень"
            Calendar.FEBRUARY -> "Лютий"
            Calendar.MARCH -> "Березень"
            Calendar.APRIL -> "Квітень"
            Calendar.MAY -> "Травень"
            Calendar.JUNE -> "Червень"
            Calendar.JULY -> "Липень"
            Calendar.AUGUST -> "Серпень"
            Calendar.SEPTEMBER -> "Вересень"
            Calendar.OCTOBER -> "Жовтень"
            Calendar.NOVEMBER -> "Листопад"
            else -> "Грудень"
        }
        _monthTitle.value = "$monthName ${cal.get(Calendar.YEAR)}"
    }
}