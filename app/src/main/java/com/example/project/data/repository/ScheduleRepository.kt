package com.example.project.data.repository

import com.example.project.data.model.Group
import com.example.project.data.model.Session
import com.example.project.data.model.Trainer
import com.example.project.data.remote.GroupsRemoteDataSource
import com.example.project.data.remote.SessionsRemoteDataSource
import com.example.project.data.remote.TrainersRemoteDataSource
import com.example.project.data.model.ScheduleUi
import java.util.Calendar

class ScheduleRepository(
    private val sessionsRemote: SessionsRemoteDataSource = SessionsRemoteDataSource(),
    private val groupsRemote: GroupsRemoteDataSource = GroupsRemoteDataSource(),
    private val trainersRemote: TrainersRemoteDataSource = TrainersRemoteDataSource()
)  {

    fun loadTrainingsForDate(
        date: Calendar,
        onResult: (List<ScheduleUi>?, String?) -> Unit
    ) {
        val start = dayStartMillis(date)
        val end = dayEndMillis(date)

        sessionsRemote.getSessionsForDay(
            dayStart = start,
            dayEnd = end,
            onSuccess = { sessions ->
                if (sessions.isEmpty()) {
                    onResult(emptyList(), null)
                    return@getSessionsForDay
                }

                // Щоб не ускладнювати: завантажимо всі активні групи і тренерів (1 раз) і зіставимо
                groupsRemote.getActiveGroups(
                    onSuccess = { groups ->
                        trainersRemote.getActiveTrainers(
                            onSuccess = { trainers ->
                                val groupMap = groups.associateBy { it.id }
                                val trainerMap = trainers.associateBy { it.id }

                                val list = sessions.mapNotNull { s ->
                                    val group = groupMap[s.groupId] ?: return@mapNotNull null
                                    val trainerId = if (s.trainerId.isNotBlank()) s.trainerId else group.trainerId
                                    val trainerName = trainerMap[trainerId]?.name.orEmpty()

                                    ScheduleUi(
                                        sessionId = s.id,
                                        groupId = s.groupId,
                                        startAt = s.startAt,
                                        endAt = s.endAt,
                                        title = group.title,
                                        trainerName = trainerName,
                                        capacity = s.capacity,
                                        isPassed = s.startAt < System.currentTimeMillis()
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
    }

    private fun dayStartMillis(date: Calendar): Long {
        val c = (date.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun dayEndMillis(date: Calendar): Long {
        val c = (date.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return c.timeInMillis
    }
}