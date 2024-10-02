package com.masterello.categoryservice.mapper

import com.masterello.categoryservice.dto.CategoryDto
import com.masterello.categoryservice.entity.Category

import org.junit.jupiter.api.Assertions.*
import org.mockito.InjectMocks
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class CategoryMapperTest {
    @InjectMocks
    private lateinit var categoryMapper: CategoryMapper

    @Test
    fun `categoryDtoToCategory should map CategoryDto to Category`() {
        val dto = CategoryDto(
            name = "Test Category",
            description = "Test Description",
            parentCode = 123,
            isService = true,
            active = true
        )

        val result = categoryMapper.categoryDtoToCategory(dto)

        assertNotNull(result)
        assertEquals(dto.name?.lowercase(), result.name)
        assertEquals(dto.name, result.originalName)
        assertEquals(dto.description, result.description)
        assertEquals(dto.parentCode, result.parentCode)
        assertEquals(dto.isService, result.isService)
        assertEquals(dto.active, result.active)
    }

    @Test
    fun `categoryDtoToCategory should throw IllegalArgumentException when dto is null`() {
        assertThrows<IllegalArgumentException> {
            categoryMapper.categoryDtoToCategory(null)
        }
    }

    @Test
    fun `mergeCategories should update Category fields with values from CategoryDto`() {
        val category = Category(
            name = "Old Name",
            originalName = "Old Name",
            description = "Old Description",
            parentCode = 111,
            isService = false,
            active = true
        )

        val dto = CategoryDto(
            name = "New Name",
            description = "New Description",
            parentCode = 222,
            isService = true,
            active = false
        )

        categoryMapper.mergeCategories(category, dto)

        assertEquals(dto.name?.lowercase(), category.name)
        assertEquals(dto.name, category.originalName)
        assertEquals(dto.description, category.description)
        assertEquals(dto.parentCode, category.parentCode)
        assertEquals(dto.isService, category.isService)
        assertEquals(dto.active, category.active)
    }

    @Test
    fun `mergeCategories should throw IllegalArgumentException when dto is null`() {
        val category = Category(
            name = "Old Name",
            originalName = "Old Name",
            description = "Old Description",
            parentCode = 111,
            isService = false,
            active = true
        )

        assertThrows<IllegalArgumentException> {
            categoryMapper.mergeCategories(category, null)
        }
    }

    @Test
    fun `categoryToCategoryDto should map Category to CategoryDto`() {
        val category = Category(
            uuid = UUID.randomUUID(),
            name = "test-name",
            originalName = "Test Category",
            description = "Test Description",
            categoryCode = 123,
            parentCode = 111,
            isService = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            active = true
        )

        val result = categoryMapper.categoryToCategoryDto(category)

        assertNotNull(result)
        assertEquals(category.uuid, result.uuid)
        assertEquals(category.originalName, result.name)
        assertEquals(category.description, result.description)
        assertEquals(category.categoryCode, result.categoryCode)
        assertEquals(category.parentCode, result.parentCode)
        assertEquals(category.isService, result.isService)
        assertEquals(category.createdDate, result.createdDate)
        assertEquals(category.updatedDate, result.updatedDate)
        assertEquals(category.active, result.active)
    }

    @Test
    fun `categoryToCategoryDto should throw IllegalArgumentException when category is null`() {
        assertThrows<IllegalArgumentException> {
            categoryMapper.categoryToCategoryDto(null)
        }
    }

}