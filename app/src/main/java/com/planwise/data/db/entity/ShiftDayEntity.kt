package com.planwise.data.db.entity

import androidx.room.Entity
import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.ShiftType

@Entity(tableName = "shift_days", primaryKeys = ["dateYyyymmdd"])
data class ShiftDayEntity(
    val dateYyyymmdd: Int,
    val shiftType: ShiftType,
    val dayStatus: DayStatus,
    val note: String?,
    val updatedAt: Long,
)
