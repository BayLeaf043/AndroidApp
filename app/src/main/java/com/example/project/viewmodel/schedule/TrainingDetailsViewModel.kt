package com.example.project.viewmodel.schedule

import com.example.project.data.repository.TrainingDetailsRepository
import com.example.project.data.repository.BookingRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.project.ui.common.Event

class TrainingDetailsViewModel(
    private val repo: TrainingDetailsRepository,
    private val bookingRepo: BookingRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,

        val metaText: String = "Вік: - , Рівень: - , Тип: -",

        val alreadyBooked: Boolean = false,

        val membershipMessage: String? = null,
        val membershipPurchaseId: String? = null,

        val singlePriceText: String? = null,
        val singleServiceId: String? = null,

        val actionInProgress: Boolean = false
    )

    private val _state = MutableLiveData(UiState())
    val state: LiveData<UiState> = _state

    fun load(
        userId: String,
        sessionId: String,
        groupId: String
    ) {
        _state.value = _state.value?.copy(loading = true, error = null)

        repo.loadGroupMeta(groupId) { meta, err ->
            if (meta == null) {
                _state.postValue(_state.value?.copy(loading = false, error = err ?: "Помилка групи"))
                return@loadGroupMeta
            }

            val metaText =
                "Вік: ${meta.ageGroup.ifBlank { "-" }}, Рівень: ${meta.level.ifBlank { "-" }}, Тип: ${meta.trainingType.ifBlank { "-" }}"

            _state.postValue(_state.value?.copy(metaText = metaText))

            // 1) already booked?
            repo.checkAlreadyBooked(userId, sessionId) { already ->
                if (already) {
                    _state.postValue(
                        _state.value?.copy(
                            loading = false,
                            alreadyBooked = true,
                            membershipMessage = "☑️ Ви вже записані на це тренування",
                            membershipPurchaseId = null,
                            singlePriceText = null,
                            singleServiceId = null
                        )
                    )
                    return@checkAlreadyBooked
                }

                // 2) membership?
                repo.findMatchingActiveMembership(userId, meta) { member, memberErr ->
                    if (memberErr != null) {
                        // якщо membership не змогли визначити — покажемо single
                    }
                    if (member != null) {
                        _state.postValue(
                            _state.value?.copy(
                                loading = false,
                                alreadyBooked = false,
                                membershipMessage = member.message,
                                membershipPurchaseId = member.purchaseId,
                                singlePriceText = null,
                                singleServiceId = null
                            )
                        )
                        return@findMatchingActiveMembership
                    }

                    // 3) single
                    repo.findSingleForTrainingType(meta.trainingType) { single, singleErr ->
                        _state.postValue(
                            _state.value?.copy(
                                loading = false,
                                alreadyBooked = false,
                                membershipMessage = null,
                                membershipPurchaseId = null,
                                singlePriceText = single?.priceText
                                    ?: "Разове тренування: ціна недоступна",
                                singleServiceId = single?.serviceId,
                                error = singleErr // можна показати тостом
                            )
                        )
                    }
                }
            }
        }
    }

    fun joinSingle(
        userId: String,
        groupId: String,
        sessionId: String
    ) {
        val st = _state.value ?: UiState()
        val serviceId = st.singleServiceId
        if (serviceId.isNullOrBlank()) {
            _state.value = st.copy(error = "Не вдалося визначити разову послугу")
            return
        }

        _state.value = st.copy(actionInProgress = true, error = null)

        bookingRepo.hasActiveBookingForSession(userId, sessionId) { already ->
            if (already) {
                _state.postValue(st.copy(actionInProgress = false, error = "☑️ Ви вже записані"))
                return@hasActiveBookingForSession
            }

            bookingRepo.createSingleBooking(userId, groupId, sessionId, serviceId) { ok, err ->
                _state.postValue(
                    _state.value?.copy(
                        actionInProgress = false,
                        error = if (ok) null else (err ?: "Помилка створення запису")
                    )
                )
                if (ok) {
                    _singleSuccess.postValue(Event(Unit))
                }
            }
        }
    }

    fun joinMembership(
        userId: String,
        groupId: String,
        sessionId: String
    ) {
        val st = _state.value ?: UiState()
        val purchaseId = st.membershipPurchaseId
        if (purchaseId.isNullOrBlank()) {
            _state.value = st.copy(error = "Немає доступного абонемента")
            return
        }

        _state.value = st.copy(actionInProgress = true, error = null)

        bookingRepo.createMembershipBookingTx(
            userId = userId,
            sessionId = sessionId,
            groupId = groupId,
            purchaseId = purchaseId
        ) { ok, err ->
            _state.postValue(
                _state.value?.copy(
                    actionInProgress = false,
                    error = if (ok) null else (err ?: "Помилка запису")
                )
            )
            if (ok) _membershipSuccess.postValue(Event(Unit))
        }
    }

    private val _singleSuccess = MutableLiveData<Event<Unit>?>()
    val singleSuccess: LiveData<Event<Unit>?> = _singleSuccess

    private val _membershipSuccess = MutableLiveData<Event<Unit>?>()
    val membershipSuccess: LiveData<Event<Unit>?> = _membershipSuccess
}