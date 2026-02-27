package com.planwise.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.planwise.domain.model.RecurrenceType

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startDateTime: Long,
    val endDateTime: Long?,
    val categoryId: String,
    val subcategoryId: String?,
    val color: Int,
    val locationText: String?,
    val description: String?,
    val recurrenceType: RecurrenceType,
    val recurrenceInterval: Int,
    val remindersMinutes: List<Int>,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean = false,
)
