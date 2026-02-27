package com.planwise.data.repo

import com.planwise.data.db.dao.CategoryDao
import com.planwise.data.mappers.toDomain
import com.planwise.domain.model.Category
import com.planwise.domain.model.Subcategory
import com.planwise.domain.repo.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeCategories().map { it.map { e -> e.toDomain() } }

    override fun observeSubcategories(): Flow<List<Subcategory>> =
        categoryDao.observeSubcategories().map { it.map { e -> e.toDomain() } }

    override suspend fun getCategoriesNow(): List<Category> =
        categoryDao.getCategoriesNow().map { it.toDomain() }

    override suspend fun getSubcategoriesNow(): List<Subcategory> =
        categoryDao.getSubcategoriesNow().map { it.toDomain() }
}
