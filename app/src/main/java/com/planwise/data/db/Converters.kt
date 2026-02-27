package com.planwise.data.db

import androidx.room.TypeConverter
import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.RecurrenceType
import com.planwise.domain.model.ShiftType

class Converters {
    @TypeConverter fun fromRecurrenceType(v: RecurrenceType): String = v.name
    @TypeConverter fun toRecurrenceType(v: String): RecurrenceType = runCatching { RecurrenceType.valueOf(v) }.getOrDefault(RecurrenceType.NONE)

    @TypeConverter fun fromShiftType(v: ShiftType): String = v.name
    @TypeConverter fun toShiftType(v: String): ShiftType = runCatching { ShiftType.valueOf(v) }.getOrDefault(ShiftType.NONE)

    @TypeConverter fun fromDayStatus(v: DayStatus): String = v.name
    @TypeConverter fun toDayStatus(v: String): DayStatus = runCatching { DayStatus.valueOf(v) }.getOrDefault(DayStatus.NORMAL)

    @TypeConverter fun fromReminders(v: List<Int>): String = v.joinToString(",")
    @TypeConverter fun toReminders(v: String): List<Int> =
        v.split(",").mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
}
