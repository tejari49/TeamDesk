package com.planwise.data.mappers

import com.planwise.data.db.entity.CategoryEntity
import com.planwise.data.db.entity.EventEntity
import com.planwise.data.db.entity.ShiftDayEntity
import com.planwise.data.db.entity.SubcategoryEntity
import com.planwise.domain.model.Category
import com.planwise.domain.model.Event
import com.planwise.domain.model.ShiftDay
import com.planwise.domain.model.Subcategory

fun EventEntity.toDomain(): Event = Event(
    id = id,
    title = title,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    categoryId = categoryId,
    subcategoryId = subcategoryId,
    color = color,
    locationText = locationText,
    description = description,
    recurrenceType = recurrenceType,
    recurrenceInterval = recurrenceInterval,
    remindersMinutes = remindersMinutes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deleted = deleted,
)

fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    categoryId = categoryId,
    subcategoryId = subcategoryId,
    color = color,
    locationText = locationText,
    description = description,
    recurrenceType = recurrenceType,
    recurrenceInterval = recurrenceInterval,
    remindersMinutes = remindersMinutes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deleted = deleted,
)

fun ShiftDayEntity.toDomain(): ShiftDay = ShiftDay(
    dateYyyymmdd = dateYyyymmdd,
    shiftType = shiftType,
    dayStatus = dayStatus,
    note = note,
    updatedAt = updatedAt,
)

fun ShiftDay.toEntity(): ShiftDayEntity = ShiftDayEntity(
    dateYyyymmdd = dateYyyymmdd,
    shiftType = shiftType,
    dayStatus = dayStatus,
    note = note,
    updatedAt = updatedAt,
)

fun CategoryEntity.toDomain(): Category = Category(id, nameKey, defaultColor)
fun SubcategoryEntity.toDomain(): Subcategory = Subcategory(id, categoryId, nameKey)
