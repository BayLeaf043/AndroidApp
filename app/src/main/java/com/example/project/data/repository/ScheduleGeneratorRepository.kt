package com.example.project.data.repository

import com.example.project.data.remote.GroupsRemoteDataSource
import com.example.project.data.remote.SessionsRemoteDataSource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import com.example.project.data.model.Group
import com.example.project.data.model.Session
import com.example.project.data.model.WeeklyScheduleItem

class ScheduleGeneratorRepository(
    private val groupsRemote: GroupsRemoteDataSource = GroupsRemoteDataSource(),
    private val sessionsRemote: SessionsRemoteDataSource = SessionsRemoteDataSource()
) {

    fun generateSessionsAhead(
        daysAhead: Int = 365,
        startFrom: Calendar = Calendar.getInstance(),
        onResult: (Boolean, String?) -> Unit
    ) {
        val start = (startFrom.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (startFrom.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, daysAhead)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        groupsRemote.getActiveGroups(
            onSuccess = { groups ->
                if (groups.isEmpty()) {
                    onResult(true, null)
                    return@getActiveGroups
                }

                // Для кожної групи читаємо weeklySchedule і генеруємо sessions
                generateForGroups(groups, start, end, mutableListOf(), 0, onResult)
            },
            onError = { e ->
                onResult(false, e.localizedMessage)
            }
        )
    }

    private fun generateForGroups(
        groups: List<Group>,
        start: Calendar,
        end: Calendar,
        allSessions: MutableList<Session>,
        index: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (index >= groups.size) {
            // Коли зібрали всі sessions -> записуємо в Firestore
            if (allSessions.isEmpty()) {
                onResult(true, null)
                return
            }

            sessionsRemote.upsertSessions(
                sessions = allSessions,
                onSuccess = { onResult(true, null) },
                onError = { onResult(false, it.localizedMessage) }
            )
            return
        }

        val group = groups[index]

        groupsRemote.getWeeklySchedule(
            groupId = group.id,
            onSuccess = { schedule ->
                val generated = buildSessionsForGroup(group, schedule, start, end)
                allSessions.addAll(generated)

                // наступна група
                generateForGroups(groups, start, end, allSessions, index + 1, onResult)
            },
            onError = { e ->
                onResult(false, e.localizedMessage)
            }
        )
    }

    /**
     * Ефективна генерація:
     * Для кожного weekly slot робимо крок +7 днів.
     */
    private fun buildSessionsForGroup(
        group: Group,
        schedule: List<WeeklyScheduleItem>,
        rangeStart: Calendar,
        rangeEnd: Calendar
    ): List<Session> {
        if (schedule.isEmpty()) return emptyList()

        val result = mutableListOf<Session>()
        val capacity = max(1, group.maxStudents)
        val trainerId = group.trainerId

        for (slot in schedule) {
            val first = firstOccurrence(rangeStart, slot.dayOfWeek)
            val (h, m) = parseTime(slot.startTime)

            val c = (first.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            while (c.timeInMillis <= rangeEnd.timeInMillis) {
                val startAt = c.timeInMillis
                val endAt = startAt + slot.durationMinutes.toLong() * 60_000L

                val id = sessionDocId(group.id, startAt)

                result.add(
                    Session(
                        id = id,
                        capacity = capacity,
                        startAt = startAt,
                        endAt = endAt,
                        groupId = group.id,
                        trainerId = trainerId,
                        status = "scheduled",
                        bookedActive = 0
                    )
                )

                c.add(Calendar.DAY_OF_YEAR, 7)
            }
        }

        return result
    }

    /**
     * Знаходимо першу дату >= rangeStart з потрібним dayOfWeek (Calendar.DAY_OF_WEEK).
     */
    private fun firstOccurrence(rangeStart: Calendar, targetDayOfWeek: Int): Calendar {
        val c = (rangeStart.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentDow = c.get(Calendar.DAY_OF_WEEK)
        val delta = (targetDayOfWeek - currentDow + 7) % 7
        c.add(Calendar.DAY_OF_YEAR, delta)
        return c
    }

    private fun parseTime(startTime: String): Pair<Int, Int> {
        val parts = startTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h to m
    }

    /**
     * Детермінований id: groupId_yyyyMMdd_HHmm
     * Це дозволяє запускати генерацію повторно без дублювань (перезапише те саме).
     */
    private fun sessionDocId(groupId: String, startAtMillis: Long): String {
        val fmt = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        return "${groupId}_${fmt.format(startAtMillis)}"
    }
}