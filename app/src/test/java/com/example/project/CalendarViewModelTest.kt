package com.example.project

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import com.example.project.viewmodel.schedule.CalendarViewModel

class CalendarViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @Test
    fun `init builds days list for current month`() {
        val vm = CalendarViewModel()

        val days = vm.days.getOrAwaitValue()
        val monthCal = vm.monthCalendar.getOrAwaitValue()

        val expectedMax = (monthCal.clone() as Calendar).getActualMaximum(Calendar.DAY_OF_MONTH)

        assertEquals(expectedMax, days.size)
        assertEquals(1, days.first().dayNumber)
        assertEquals(expectedMax, days.last().dayNumber)

        // усі DayUi календарі мають бути в цьому місяці/році
        val y = monthCal.get(Calendar.YEAR)
        val m = monthCal.get(Calendar.MONTH)

        assertTrue(days.all {
            it.calendar.get(Calendar.YEAR) == y && it.calendar.get(Calendar.MONTH) == m
        })
    }

    @Test
    fun `init selects today if same month`() {
        val today = Calendar.getInstance()
        val vm = CalendarViewModel()

        val selectedIndex = vm.selectedIndex.getOrAwaitValue()
        val selectedDate = vm.selectedDate.getOrAwaitValue()
        val monthCal = vm.monthCalendar.getOrAwaitValue()

        val sameMonth = today.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH)

        if (sameMonth) {
            val expectedIndex = (today.get(Calendar.DAY_OF_MONTH) - 1)
            assertEquals(expectedIndex, selectedIndex)

            assertEquals(today.get(Calendar.YEAR), selectedDate.get(Calendar.YEAR))
            assertEquals(today.get(Calendar.MONTH), selectedDate.get(Calendar.MONTH))
            assertEquals(today.get(Calendar.DAY_OF_MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
        } else {
            assertEquals(0, selectedIndex)
            assertEquals(1, selectedDate.get(Calendar.DAY_OF_MONTH))
        }
    }

    @Test
    fun `selectDay updates selectedIndex and selectedDate`() {
        val vm = CalendarViewModel()
        val days = vm.days.getOrAwaitValue()

        val target = 4 // 5-й день місяця
        vm.selectDay(target)

        val selectedIndex = vm.selectedIndex.getOrAwaitValue()
        val selectedDate = vm.selectedDate.getOrAwaitValue()

        assertEquals(target, selectedIndex)
        assertEquals(days[target].dayNumber, selectedDate.get(Calendar.DAY_OF_MONTH))
        assertEquals(days[target].calendar.get(Calendar.MONTH), selectedDate.get(Calendar.MONTH))
        assertEquals(days[target].calendar.get(Calendar.YEAR), selectedDate.get(Calendar.YEAR))
    }

    @Test
    fun `selectDay out of range does not change selectedDate`() {
        val vm = CalendarViewModel()

        val before = vm.selectedDate.getOrAwaitValue()
        vm.selectDay(10_000) // out of range

        val after = vm.selectedDate.getOrAwaitValue()

        assertEquals(before.timeInMillis, after.timeInMillis)
    }

    @Test
    fun `nextMonth moves monthCalendar forward and selects first day`() {
        val vm = CalendarViewModel()

        val beforeMonth = vm.monthCalendar.getOrAwaitValue()
        val beforeYear = beforeMonth.get(Calendar.YEAR)
        val beforeM = beforeMonth.get(Calendar.MONTH)

        vm.nextMonth()

        val afterMonth = vm.monthCalendar.getOrAwaitValue()
        val afterM = afterMonth.get(Calendar.MONTH)
        val afterYear = afterMonth.get(Calendar.YEAR)

        val expected = (beforeMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        assertEquals(expected.get(Calendar.YEAR), afterYear)
        assertEquals(expected.get(Calendar.MONTH), afterM)
        assertEquals(1, afterMonth.get(Calendar.DAY_OF_MONTH))

        val selectedIndex = vm.selectedIndex.getOrAwaitValue()
        val selectedDate = vm.selectedDate.getOrAwaitValue()

        assertEquals(0, selectedIndex)
        assertEquals(1, selectedDate.get(Calendar.DAY_OF_MONTH))
        assertEquals(afterYear, selectedDate.get(Calendar.YEAR))
        assertEquals(afterM, selectedDate.get(Calendar.MONTH))

        assertTrue(afterYear != beforeYear || afterM != beforeM)
    }

    @Test
    fun `prevMonth moves monthCalendar backward and selects first day`() {
        val vm = CalendarViewModel()

        val beforeMonth = vm.monthCalendar.getOrAwaitValue()

        vm.prevMonth()

        val afterMonth = vm.monthCalendar.getOrAwaitValue()

        val expected = (beforeMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        assertEquals(expected.get(Calendar.YEAR), afterMonth.get(Calendar.YEAR))
        assertEquals(expected.get(Calendar.MONTH), afterMonth.get(Calendar.MONTH))
        assertEquals(1, afterMonth.get(Calendar.DAY_OF_MONTH))

        val selectedIndex = vm.selectedIndex.getOrAwaitValue()
        val selectedDate = vm.selectedDate.getOrAwaitValue()

        assertEquals(0, selectedIndex)
        assertEquals(1, selectedDate.get(Calendar.DAY_OF_MONTH))
        assertEquals(afterMonth.get(Calendar.YEAR), selectedDate.get(Calendar.YEAR))
        assertEquals(afterMonth.get(Calendar.MONTH), selectedDate.get(Calendar.MONTH))
    }
}