package com.planwise.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteEvent(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    @SerialName("start_datetime") val startDateTime: Long,
    @SerialName("end_datetime") val endDateTime: Long? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("subcategory_id") val subcategoryId: String? = null,
    val color: Int,
    @SerialName("location_text") val locationText: String? = null,
    val description: String? = null,
    @SerialName("recurrence_type") val recurrenceType: String,
    @SerialName("recurrence_interval") val recurrenceInterval: Int,
    val reminders: List<Int>,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
    val deleted: Boolean = false,
)

@Serializable
data class RemoteShiftDay(
    val date: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("shift_type") val shiftType: String,
    @SerialName("day_status") val dayStatus: String,
    val note: String? = null,
    @SerialName("updated_at") val updatedAt: Long,
)
