package com.example.project.data.remote

import com.example.project.data.model.Group
import com.example.project.data.model.WeeklyScheduleItem

class GroupsRemoteDataSource {
    private val db = FirebaseProvider.db

    fun getActiveGroups(
        onSuccess: (List<Group>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.GROUPS)
            .whereEqualTo("isActive", true)
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(Group::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }

    fun getWeeklySchedule(
        groupId: String,
        onSuccess: (List<WeeklyScheduleItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.GROUPS)
            .document(groupId)
            .collection(FirestorePaths.WEEKLY_SCHEDULE)
            .get()
            .handle(
                onSuccess = { qs ->
                    val list = qs.documents.mapNotNull { d ->
                        d.toObject(WeeklyScheduleItem::class.java)?.copy(id = d.id)
                    }
                    onSuccess(list)
                },
                onError = onError
            )
    }
}