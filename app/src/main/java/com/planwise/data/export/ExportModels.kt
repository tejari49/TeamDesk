package com.planwise.data.export

import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.RecurrenceType
import com.planwise.domain.model.ShiftType
import kotlinx.serialization.Serializable

@Serializable
data class ExportBundle(
    val version: Int = 1,
    val exportedAt: Long,
    val events: List<ExportEvent>,
    val shifts: List<ExportShiftDay>,
)

@Serializable
data class ExportEvent(
    val id: String,
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
    val deleted: Boolean,
)

@Serializable
data class ExportShiftDay(
    val dateYyyymmdd: Int,
    val shiftType: ShiftType,
    val dayStatus: DayStatus,
    val note: String?,
    val updatedAt: Long,
)
