package com.planwise.data.seed

import com.planwise.data.db.entity.CategoryEntity
import com.planwise.data.db.entity.SubcategoryEntity

object SeedData {
    private const val BLUE = 0xFF3F51B5.toInt()
    private const val GREEN = 0xFF4CAF50.toInt()
    private const val ORANGE = 0xFFFF9800.toInt()
    private const val PURPLE = 0xFF9C27B0.toInt()
    private const val TEAL = 0xFF009688.toInt()
    private const val GREY = 0xFF607D8B.toInt()

    val categories = listOf(
        CategoryEntity("health", "cat_health", BLUE),
        CategoryEntity("personal", "cat_personal", GREEN),
        CategoryEntity("work", "cat_work", ORANGE),
        CategoryEntity("family_edu", "cat_family_edu", PURPLE),
        CategoryEntity("travel", "cat_travel", TEAL),
        CategoryEntity("other", "cat_other", GREY),
    )

    val subcategories = listOf(
        SubcategoryEntity("doctor", "health", "sub_doctor"),
        SubcategoryEntity("dentist", "health", "sub_dentist"),
        SubcategoryEntity("lab", "health", "sub_lab"),

        SubcategoryEntity("birthday", "personal", "sub_birthday"),
        SubcategoryEntity("anniversary", "personal", "sub_anniversary"),
        SubcategoryEntity("party", "personal", "sub_party"),
        SubcategoryEntity("friends", "personal", "sub_friends"),

        SubcategoryEntity("meeting", "work", "sub_meeting"),
        SubcategoryEntity("presentation", "work", "sub_presentation"),
        SubcategoryEntity("deadline", "work", "sub_deadline"),

        SubcategoryEntity("parent_evening", "family_edu", "sub_parent_evening"),
        SubcategoryEntity("course", "family_edu", "sub_course"),
        SubcategoryEntity("kids", "family_edu", "sub_kids"),

        SubcategoryEntity("vacation_start", "travel", "sub_vacation_start"),
        SubcategoryEntity("airport_checkin", "travel", "sub_airport_checkin"),
        SubcategoryEntity("return_trip", "travel", "sub_return_trip"),

        SubcategoryEntity("pay_bills", "other", "sub_pay_bills"),
        SubcategoryEntity("clean_home", "other", "sub_clean_home"),
        SubcategoryEntity("bank", "other", "sub_bank"),
        SubcategoryEntity("shopping", "other", "sub_shopping"),
    )
}
