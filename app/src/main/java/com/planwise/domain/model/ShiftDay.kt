package com.planwise.domain.model

data class ShiftDay(
    val dateYyyymmdd: Int,
    val shiftType: ShiftType,
    val dayStatus: DayStatus,
    val note: String?,
    val updatedAt: Long,
)
