package com.planwise.domain.repo

import com.planwise.domain.model.Category
import com.planwise.domain.model.Subcategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeSubcategories(): Flow<List<Subcategory>>
    suspend fun getCategoriesNow(): List<Category>
    suspend fun getSubcategoriesNow(): List<Subcategory>
}
