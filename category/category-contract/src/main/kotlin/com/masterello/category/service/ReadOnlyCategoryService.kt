package com.masterello.category.service

import com.masterello.category.dto.CategoryBulkRequest
import com.masterello.category.dto.CategoryDto
import java.util.*

interface ReadOnlyCategoryService {
    fun getAllChildCategories(categoryCode: Int): List<CategoryDto>
    fun getAllChildCategoriesBulk(categoryRequest: CategoryBulkRequest): Map<Int, List<CategoryDto>>
    fun getAllParentCategoriesBulk(categoryRequest: CategoryBulkRequest): Map<Int, List<CategoryDto>>
    fun getAllParentCategories(categoryCode: Int): List<CategoryDto>
    fun getAllCategories(): List<CategoryDto>
    fun getCategoryById(id: UUID): CategoryDto
    fun getCategoryByName(name: String): CategoryDto?
}