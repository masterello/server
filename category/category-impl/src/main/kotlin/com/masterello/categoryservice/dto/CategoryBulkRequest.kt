package com.masterello.categoryservice.dto

data class CategoryBulkRequest (
        val categoryCodes: List<Int>,
        val serviceOnly: Boolean
)
