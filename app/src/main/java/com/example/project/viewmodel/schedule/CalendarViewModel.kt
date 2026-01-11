package com.example.project.viewmodel.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.project.data.model.DayUi
import androidx.lifecycle.LiveData
import java.util.Calendar

class CalendarViewModel: ViewModel() {

    private val _days = MutableLiveData<List<DayUi>>()
    val days: LiveData<List<DayUi>> = _days

    private val _selectedIndex = MutableLiveData(0)
    val selectedIndex: LiveData<Int> = _selectedIndex

    private val _selectedDate = MutableLiveData<Calendar>()
    val selectedDate: LiveData<Calendar> = _selectedDate

    private val _monthCalendar = MutableLiveData(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    })
    val monthCalendar: LiveData<Calendar> = _monthCalendar

    init {
        rebuildDays(selectTodayIfPossible = true)
    }

    fun prevMonth() {
        val cal = (_monthCalendar.value ?: Calendar.getInstance()).clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        _monthCalendar.value = cal
        _selectedIndex.value = 0
        rebuildDays(selectTodayIfPossible = true)
    }

    fun nextMonth() {
        val cal = (_monthCalendar.value ?: Calendar.getInstance()).clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        _monthCalendar.value = cal
        _selectedIndex.value = 0
        rebuildDays(selectTodayIfPossible = true)
    }

    fun selectDay(index: Int) {
        _selectedIndex.value = index
        val list = _days.value.orEmpty()
        if (index in list.indices) {
            _selectedDate.value = list[index].calendar
        }
    }

    private fun rebuildDays(selectTodayIfPossible: Boolean) {
        val monthCal = (_monthCalendar.value ?: Calendar.getInstance()).clone() as Calendar
        val maxDay = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = (1..maxDay).map { day ->
            val c = (monthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            DayUi(
                calendar = c,
                dayNameShort = dayNameShort(c), // "пн", "вт"...
                dayNumber = day
            )
        }
        _days.value = list

        // ✅ вибираємо сьогодні, якщо поточний місяць, інакше перший день
        val newIndex = if (selectTodayIfPossible) {
            val today = Calendar.getInstance()
            val sameMonth = today.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH)

            if (sameMonth) (today.get(Calendar.DAY_OF_MONTH) - 1).coerceIn(0, list.size - 1)
            else 0
        } else 0

        _selectedIndex.value = newIndex
        _selectedDate.value = list[newIndex].calendar
    }

    private fun dayNameShort(c: Calendar): String {
        return when (c.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "пн"
            Calendar.TUESDAY -> "вт"
            Calendar.WEDNESDAY -> "ср"
            Calendar.THURSDAY -> "чт"
            Calendar.FRIDAY -> "пт"
            Calendar.SATURDAY -> "сб"
            else -> "нд"
        }
    }



}