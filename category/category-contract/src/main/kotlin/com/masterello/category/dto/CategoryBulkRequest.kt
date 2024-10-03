package com.masterello.category.dto

data class CategoryBulkRequest (
        val categoryCodes: List<Int>,
        val serviceOnly: Boolean
)
