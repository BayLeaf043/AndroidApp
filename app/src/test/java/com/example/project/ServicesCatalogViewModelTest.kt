package com.example.project

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.project.viewmodel.service.ServicesCatalogViewModel
import com.example.project.data.repository.ServicesRepository
import com.example.project.data.model.Service
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

class ServicesCatalogViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var repo: ServicesRepository
    private lateinit var vm: ServicesCatalogViewModel

    private val services = listOf(
        Service(
            id = "s1",
            title = "Абонемент Pole Basic",
            kind = "membership",
            trainingType = "pole",
            level = "basic",
            sessionsCount = 8,
            price = 1200
        ),
        Service(
            id = "s2",
            title = "Абонемент Pole Pro",
            kind = "membership",
            trainingType = "pole",
            level = "pro",
            sessionsCount = 8,
            price = 1500
        ),
        Service(
            id = "s3",
            title = "Абонемент Stretching Basic",
            kind = "membership",
            trainingType = "stretching",
            level = "basic",
            sessionsCount = 12,
            price = 1100
        )
    )

    @Before
    fun setup() {
        repo = mockk()

        every { repo.loadMembershipServices(any()) } answers {
            val cb = firstArg<(List<Service>?, String?) -> Unit>()
            cb(services, null)
        }

        vm = ServicesCatalogViewModel(repo)
    }

    @Test
    fun `loadMemberships success - puts list into memberships`() {
        vm.loadMemberships()

        val list = vm.memberships.getOrAwaitValue()
        assertEquals(3, list.size)
        assertEquals("s1", list[0].id)
    }

    @Test
    fun `setLevelFilter basic - returns only basic`() {
        vm.loadMemberships()

        vm.setLevelFilter("basic")
        val list = vm.memberships.getOrAwaitValue()

        assertEquals(2, list.size)
        assertTrue(list.all { it.level.equals("basic", ignoreCase = true) })
    }

    @Test
    fun `setLevelFilter pro - returns only pro`() {
        vm.loadMemberships()

        vm.setLevelFilter("pro")
        val list = vm.memberships.getOrAwaitValue()

        assertEquals(1, list.size)
        assertEquals("s2", list.first().id)
    }

    @Test
    fun `setSearchQuery matches title - filters by title`() {
        vm.loadMemberships()

        vm.setSearchQuery("stretch")
        val list = vm.memberships.getOrAwaitValue()

        assertEquals(1, list.size)
        assertEquals("s3", list.first().id)
    }

    @Test
    fun `setSearchQuery matches trainingType - filters by trainingType`() {
        vm.loadMemberships()

        vm.setSearchQuery("pole")
        val list = vm.memberships.getOrAwaitValue()

        assertEquals(2, list.size)
        assertTrue(list.all { it.trainingType.equals("pole", true) })
    }

    @Test
    fun `search is case-insensitive and trimmed`() {
        vm.loadMemberships()

        vm.setSearchQuery("  PoLe  ")
        val list = vm.memberships.getOrAwaitValue()

        assertEquals(2, list.size)
    }

    @Test
    fun `combined filters - level basic + search pole = only Pole Basic`() {
        vm.loadMemberships()

        vm.setLevelFilter("basic")
        vm.setSearchQuery("pole")

        val list = vm.memberships.getOrAwaitValue()
        assertEquals(1, list.size)
        assertEquals("s1", list.first().id)
    }

    @Test
    fun `loadMemberships error - emits errorMessage and keeps memberships empty`() {
        every { repo.loadMembershipServices(any()) } answers {
            val cb = firstArg<(List<Service>?, String?) -> Unit>()
            cb(null, "Помилка завантаження")
        }

        vm.loadMemberships()

        val list = vm.memberships.getOrAwaitValue()
        assertTrue(list.isEmpty())

        val event = vm.errorMessage.getOrAwaitValue()
        assertNotNull(event)

        val msg = event?.getContentIfNotHandled()
        assertEquals("Помилка завантаження", msg)
    }
}