package com.planwise.data.seed

import com.planwise.data.db.dao.CategoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SeedInitializer @Inject constructor(
    private val categoryDao: CategoryDao,
) {
    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (categoryDao.countCategories() == 0) {
            categoryDao.insertCategories(SeedData.categories)
            categoryDao.insertSubcategories(SeedData.subcategories)
        }
    }
}
