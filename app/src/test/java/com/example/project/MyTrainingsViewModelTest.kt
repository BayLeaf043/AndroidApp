package com.example.project

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import com.example.project.data.model.MyTrainingUi
import com.example.project.data.repository.MyTrainingsRepository
import com.example.project.viewmodel.mytraining.MyTrainingsViewModel

class MyTrainingsViewModelTest {
    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val repo = mockk<MyTrainingsRepository>(relaxed = true)

    @Test
    fun `init sets monthTitle`() {
        val vm = MyTrainingsViewModel(repo)

        vm.init()

        val title = vm.monthTitle.value
        assertNotNull(title)
        assertTrue(title!!.isNotBlank())

        val year = Calendar.getInstance().get(Calendar.YEAR).toString()
        assertTrue(title.contains(year))
    }

    @Test
    fun `prevMonth updates monthCal and monthTitle`() {
        val vm = MyTrainingsViewModel(repo)
        vm.init()

        val before = vm.monthCal.value!!.clone() as Calendar
        val beforeTitle = vm.monthTitle.value

        vm.prevMonth()

        val after = vm.monthCal.value!!
        val afterTitle = vm.monthTitle.value

        // monthCal має бути 1 число місяця
        assertEquals(1, after.get(Calendar.DAY_OF_MONTH))

        // місяць змінився на -1 (може змінитися і рік на межі)
        val expected = (before.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        assertEquals(expected.get(Calendar.YEAR), after.get(Calendar.YEAR))
        assertEquals(expected.get(Calendar.MONTH), after.get(Calendar.MONTH))

        assertNotNull(afterTitle)
        assertTrue(afterTitle!!.isNotBlank())
        assertNotEquals(beforeTitle, afterTitle)
    }

    @Test
    fun `nextMonth updates monthCal and monthTitle`() {
        val vm = MyTrainingsViewModel(repo)
        vm.init()

        val before = vm.monthCal.value!!.clone() as Calendar
        val beforeTitle = vm.monthTitle.value

        vm.nextMonth()

        val after = vm.monthCal.value!!
        val afterTitle = vm.monthTitle.value

        assertEquals(1, after.get(Calendar.DAY_OF_MONTH))

        val expected = (before.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        assertEquals(expected.get(Calendar.YEAR), after.get(Calendar.YEAR))
        assertEquals(expected.get(Calendar.MONTH), after.get(Calendar.MONTH))

        assertNotNull(afterTitle)
        assertTrue(afterTitle!!.isNotBlank())
        assertNotEquals(beforeTitle, afterTitle)
    }

    @Test
    fun `load success posts items and clears error`() {
        val vm = MyTrainingsViewModel(repo)
        vm.init()

        val uid = "u1"
        val sample = listOf(
            MyTrainingUi(
                bookingId = "b1",
                sessionId = "s1",
                groupId = "g1",
                title = "Pole Basic",
                trainerName = "Катя",
                startAt = 1000L,
                endAt = 2000L,
                source = "membership",
                status = "active",
                isPassed = false
            )
        )

        val cbSlot = slot<(List<MyTrainingUi>?, String?) -> Unit>()

        every {
            repo.loadMyTrainingsForMonth(eq(uid), any(), capture(cbSlot))
        } answers {
            // нічого одразу — ViewModel поставить loading=true
        }

        vm.load(uid)

        assertEquals(true, vm.loading.value)
        assertEquals(null, vm.error.value)

        cbSlot.captured.invoke(sample, null)

        assertEquals(false, vm.loading.value)
        assertEquals(null, vm.error.value)
        assertEquals(sample, vm.items.value)

        verify(exactly = 1) { repo.loadMyTrainingsForMonth(eq(uid), any(), any()) }
    }

    @Test
    fun `load error posts error and empty items`() {
        val vm = MyTrainingsViewModel(repo)
        vm.init()

        val uid = "u1"

        val cbSlot = slot<(List<MyTrainingUi>?, String?) -> Unit>()

        every {
            repo.loadMyTrainingsForMonth(eq(uid), any(), capture(cbSlot))
        } answers { }

        vm.load(uid)

        assertEquals(true, vm.loading.value)

        cbSlot.captured.invoke(null, "Network error")

        assertEquals(false, vm.loading.value)
        assertEquals("Network error", vm.error.value)
        assertEquals(emptyList<MyTrainingUi>(), vm.items.value)

        verify(exactly = 1) { repo.loadMyTrainingsForMonth(eq(uid), any(), any()) }
    }

    @Test
    fun `load passes monthCal from viewModel to repository`() {
        val vm = MyTrainingsViewModel(repo)
        vm.init()

        val uid = "u1"

        val calSlot = slot<Calendar>()
        val cbSlot = slot<(List<MyTrainingUi>?, String?) -> Unit>()

        every {
            repo.loadMyTrainingsForMonth(eq(uid), capture(calSlot), capture(cbSlot))
        } answers { }

        vm.load(uid)

        val passed = calSlot.captured
        val current = vm.monthCal.value!!

        assertEquals(current.get(Calendar.YEAR), passed.get(Calendar.YEAR))
        assertEquals(current.get(Calendar.MONTH), passed.get(Calendar.MONTH))
        assertEquals(1, passed.get(Calendar.DAY_OF_MONTH))
    }
}