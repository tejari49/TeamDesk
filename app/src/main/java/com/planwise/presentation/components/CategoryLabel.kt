package com.planwise.presentation.components

import android.content.Context
import com.planwise.domain.model.Category
import com.planwise.domain.model.Subcategory

fun Context.resolveStringKey(key: String): String {
    val id = resources.getIdentifier(key, "string", packageName)
    return if (id != 0) getString(id) else key
}

fun resolveCategoryName(context: Context, categories: List<Category>, id: String): String {
    val cat = categories.firstOrNull { it.id == id } ?: return id
    return context.resolveStringKey(cat.nameKey)
}

fun resolveSubcategoryName(context: Context, subs: List<Subcategory>, id: String?): String {
    if (id.isNullOrBlank()) return ""
    val s = subs.firstOrNull { it.id == id } ?: return id
    return context.resolveStringKey(s.nameKey)
}
