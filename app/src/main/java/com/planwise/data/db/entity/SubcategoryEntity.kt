package com.planwise.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcategories")
data class SubcategoryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val nameKey: String,
)
