package com.planwise.domain.model

data class Event(
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
    val deleted: Boolean = false,
)
