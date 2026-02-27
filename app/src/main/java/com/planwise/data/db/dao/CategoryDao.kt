package com.planwise.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.planwise.data.db.entity.CategoryEntity
import com.planwise.data.db.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM subcategories")
    fun observeSubcategories(): Flow<List<SubcategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcategories(items: List<SubcategoryEntity>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesNow(): List<CategoryEntity>

    @Query("SELECT * FROM subcategories")
    suspend fun getSubcategoriesNow(): List<SubcategoryEntity>
}
