package com.example.project.data.repository

import com.example.project.data.remote.BookingsRemoteDataSource
import com.example.project.data.remote.GroupsRemoteDataSource
import com.example.project.data.remote.SessionsRemoteDataSource
import com.example.project.data.remote.TrainersRemoteDataSource
import java.util.Calendar
import com.example.project.data.model.MyTrainingUi

class MyTrainingsRepository(
    private val bookingsRemote: BookingsRemoteDataSource = BookingsRemoteDataSource(),
    private val sessionsRemote: SessionsRemoteDataSource = SessionsRemoteDataSource(),
    private val groupsRemote: GroupsRemoteDataSource = GroupsRemoteDataSource(),
    private val trainersRemote: TrainersRemoteDataSource = TrainersRemoteDataSource()
) {

    fun loadMyTrainingsForMonth(
        userId: String,
        monthCal: Calendar,
        onResult: (List<MyTrainingUi>?, String?) -> Unit
    ) {
        val monthStart = monthStartMillis(monthCal)
        val monthEnd = monthEndMillis(monthCal)
        val now = System.currentTimeMillis()

        bookingsRemote.getBookingsForUser(
            userId = userId,
            onSuccess = { bookings ->
                val activeBookings = bookings
                    .filter { it.status.equals("active", true) }
                    .filter { it.sessionId.isNotBlank() }

                if (activeBookings.isEmpty()) {
                    onResult(emptyList(), null)
                    return@getBookingsForUser
                }

                val sessionIds = activeBookings.map { it.sessionId }.distinct()

                // 1) підтягуємо sessions
                sessionsRemote.getSessionsByIds(
                    sessionIds = sessionIds,
                    onSuccess = { sessions ->
                        val sessionMap = sessions.associateBy { it.id }

                        // 2) фільтруємо booking по sessions у межах місяця
                        val filtered = activeBookings.mapNotNull { b ->
                            val s = sessionMap[b.sessionId] ?: return@mapNotNull null
                            if (s.startAt < monthStart || s.startAt > monthEnd) return@mapNotNull null
                            b to s
                        }

                        if (filtered.isEmpty()) {
                            onResult(emptyList(), null)
                            return@getSessionsByIds
                        }

                        // 3) групи + тренери (як у ScheduleRepository)
                        groupsRemote.getActiveGroups(
                            onSuccess = { groups ->
                                trainersRemote.getActiveTrainers(
                                    onSuccess = { trainers ->
                                        val groupMap = groups.associateBy { it.id }
                                        val trainerMap = trainers.associateBy { it.id }

                                        val list = filtered.mapNotNull { (b, s) ->
                                            val g = groupMap[b.groupId] ?: return@mapNotNull null
                                            val trainerId = if (s.trainerId.isNotBlank()) s.trainerId else g.trainerId
                                            val trainerName = trainerMap[trainerId]?.name.orEmpty()

                                            MyTrainingUi(
                                                bookingId = b.id,
                                                sessionId = b.sessionId,
                                                groupId = b.groupId,
                                                title = g.title,
                                                trainerName = trainerName,
                                                startAt = s.startAt,
                                                endAt = s.endAt,
                                                source = b.source,
                                                status = b.status,
                                                isPassed = s.startAt < now
                                            )
                                        }.sortedBy { it.startAt }

                                        onResult(list, null)
                                    },
                                    onError = { onResult(null, it.localizedMessage) }
                                )
                            },
                            onError = { onResult(null, it.localizedMessage) }
                        )
                    },
                    onError = { onResult(null, it.localizedMessage) }
                )
            },
            onError = { e -> onResult(null, e.localizedMessage) }
        )
    }

    private fun monthStartMillis(cal: Calendar): Long {
        val c = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun monthEndMillis(cal: Calendar): Long {
        val c = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return c.timeInMillis
    }
}