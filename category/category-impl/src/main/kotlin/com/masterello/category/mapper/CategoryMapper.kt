package com.masterello.category.mapper

import com.masterello.category.dto.CategoryDto
import com.masterello.category.entity.Category
import org.springframework.stereotype.Service

@Service
class CategoryMapper {

    fun categoryDtoToCategory(dto: CategoryDto?): Category {
        if (dto == null) {
            throw IllegalArgumentException("CategoryDto cannot be null")
        }

        return Category(
            name = dto.name?.lowercase(),
            originalName = dto.name,
            description = dto.description,
            parentCode = dto.parentCode,
            isService = dto.isService,
            active = dto.active
        )
    }

    fun mergeCategories(category: Category, dto: CategoryDto?) {
        if (dto == null) {
            throw IllegalArgumentException("CategoryDto cannot be null")
        }

        category.name = dto.name?.lowercase()
        category.originalName = dto.name
        category.description = dto.description
        category.parentCode = dto.parentCode
        category.isService = dto.isService
        category.active = dto.active
    }

    fun categoryToCategoryDto(category: Category?): CategoryDto {
        if (category == null) {
            throw IllegalArgumentException("Category cannot be null")
        }

        return CategoryDto(
            uuid = category.uuid,
            name = category.originalName,
            description = category.description,
            categoryCode = category.categoryCode,
            parentCode = category.parentCode,
            isService = category.isService,
            createdDate = category.createdDate,
            updatedDate = category.updatedDate,
            active = category.active
        )
    }

}