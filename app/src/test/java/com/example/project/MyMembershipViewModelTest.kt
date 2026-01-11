package com.example.project

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import com.example.project.data.repository.MyMembershipRepository
import com.example.project.data.repository.PurchaseRepository
import com.example.project.viewmodel.membership.MyMembershipViewModel
import com.example.project.data.model.MyMembershipUi


class MyMembershipViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val repo = mockk<MyMembershipRepository>()
    private val purchaseRepo = mockk<PurchaseRepository>()

    private fun makeVm(): MyMembershipViewModel {
        return MyMembershipViewModel(
            repository = repo,
            purchaseRepository = purchaseRepo
        )
    }

    @Test
    fun `load calls refresh then loadMyMemberships and posts items`() {
        val vm = makeVm()
        val uid = "u1"

        val refreshSlot = slot<(String?) -> Unit>()
        val loadSlot = slot<(List<MyMembershipUi>?, String?) -> Unit>()

        val sample = listOf(
            MyMembershipUi(
                purchaseId = "p1",
                serviceId = "s1",
                title = "Pole Basic",
                trainingType = "pole",
                level = "basic",
                ageGroup = "18+",
                startAtMillis = 0L,
                endAtMillis = 0L,
                visitsTotal = 10,
                visitsUsed = 2,
                status = "active"
            )
        )

        every { repo.refreshFinishedMemberships(eq(uid), capture(refreshSlot)) } answers { /* do nothing */ }
        every { repo.loadMyMemberships(eq(uid), capture(loadSlot)) } answers { /* do nothing */ }

        vm.load(uid)

        assertEquals(true, vm.loading.value)
        assertEquals(null, vm.error.value)
        assertEquals(false, vm.activationSuccess.value)

        refreshSlot.captured.invoke(null)

        verify(exactly = 1) { repo.loadMyMemberships(eq(uid), any()) }

        loadSlot.captured.invoke(sample, null)

        assertEquals(false, vm.loading.value)
        assertEquals(null, vm.error.value)
        assertEquals(sample, vm.items.value)
    }

    @Test
    fun `load - refresh error is posted but does not block loading memberships`() {
        val vm = makeVm()
        val uid = "u1"

        val refreshSlot = slot<(String?) -> Unit>()
        val loadSlot = slot<(List<MyMembershipUi>?, String?) -> Unit>()

        every { repo.refreshFinishedMemberships(eq(uid), capture(refreshSlot)) } answers { }
        every { repo.loadMyMemberships(eq(uid), capture(loadSlot)) } answers { }

        vm.load(uid)

        refreshSlot.captured.invoke("Refresh warning")

        assertEquals("Refresh warning", vm.error.value)

        verify(exactly = 1) { repo.loadMyMemberships(eq(uid), any()) }

        loadSlot.captured.invoke(null, "Load error")

        assertEquals(false, vm.loading.value)
        assertEquals("Load error", vm.error.value)
        assertEquals(emptyList<MyMembershipUi>(), vm.items.value)
    }

    @Test
    fun `loadArchive posts items from archive`() {
        val vm = makeVm()
        val uid = "u1"

        val slot = slot<(List<MyMembershipUi>?, String?) -> Unit>()
        val sample = listOf(
            MyMembershipUi(
                purchaseId = "p2",
                serviceId = "s2",
                title = "Exotic Pro",
                trainingType = "exotic",
                level = "pro",
                ageGroup = "18+",
                startAtMillis = 1L,
                endAtMillis = 2L,
                visitsTotal = 8,
                visitsUsed = 8,
                status = "finished"
            )
        )

        every { repo.loadArchiveMemberships(eq(uid), capture(slot)) } answers { }

        vm.loadArchive(uid)

        assertEquals(true, vm.loading.value)
        assertEquals(null, vm.error.value)

        slot.captured.invoke(sample, null)

        assertEquals(false, vm.loading.value)
        assertEquals(null, vm.error.value)
        assertEquals(sample, vm.items.value)
    }

    @Test
    fun `activateToday fail sets error and does not trigger load`() {
        val vm = makeVm()
        val purchaseId = "p1"
        val uidToReload = "u1"

        val activateSlot = slot<(Boolean, String?) -> Unit>()

        every {
            purchaseRepo.activateMembershipToday(eq(purchaseId), eq(30), capture(activateSlot))
        } answers { }

        vm.activateToday(purchaseId = purchaseId, durationDays = 30, uidToReload = uidToReload)

        activateSlot.captured.invoke(false, "Server error")

        assertEquals(null, vm.activatingId.value)
        assertEquals(false, vm.activationSuccess.value)
        assertEquals("Server error", vm.error.value)

        verify(exactly = 0) { repo.refreshFinishedMemberships(any(), any()) }
        verify(exactly = 0) { repo.loadMyMemberships(any(), any()) }
    }
}