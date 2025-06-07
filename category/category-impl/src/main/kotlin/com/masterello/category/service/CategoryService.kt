package com.masterello.category.service

import com.masterello.category.dto.CategoryBulkRequest
import com.masterello.category.dto.CategoryDto
import com.masterello.category.event.CategoriesChangedEvent
import com.masterello.category.exception.CategoryAlreadyExistsException
import com.masterello.category.exception.NotFoundException
import com.masterello.category.mapper.CategoryMapper
import com.masterello.category.repository.CategoryRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class CategoryService(
        private val categoryRepository: CategoryRepository,
        private val categoryMapper: CategoryMapper,
        val publisher: ApplicationEventPublisher,
) : ReadOnlyCategoryService {

    private val log = KotlinLogging.logger {}

    override fun getAllChildCategories(categoryCode: Int): List<CategoryDto> {
        log.info { "${"Fetching all child categories for categoryCode: {}"} $categoryCode" }
        val categories = categoryRepository.findAllChildsByCategoryCode(categoryCode)

        val categoryDtos = categories.map { categoryMapper.categoryToCategoryDto(it) }
        log.info { "Fetched ${categoryDtos.size} child categories for categoryCode: $categoryCode" }
        return categoryDtos
    }

    override fun getAllChildCategoriesBulk(categoryRequest: CategoryBulkRequest): Map<Int,List<CategoryDto>> {
        log.info { "Fetching all child categories for categoryCodes: ${categoryRequest.categoryCodes}" }

        return categoryRequest.categoryCodes.associateWith { code ->
            categoryRepository.findAllChildsByCategoryCode(code)
                    .filter { !categoryRequest.serviceOnly || it.isService == true }
                    .map(categoryMapper::categoryToCategoryDto)
        }.also {
            log.info { "Fetched child categories in bulk" }
        }
    }

    override fun getAllParentCategoriesBulk(categoryRequest: CategoryBulkRequest): Map<Int,List<CategoryDto>> {
        log.info { "${"Fetching all parent categories for categoryCodes: {}"} ${categoryRequest.categoryCodes}" }
        val bulkCategories = categoryRequest.categoryCodes.associateWith { code ->

            val categories = if (categoryRequest.serviceOnly) {
                categoryRepository.findAllServiceParentsByCategoryCode(code)
            } else {
                categoryRepository.findAllParentsByCategoryCode(code)
            }
            categories.map { categoryMapper.categoryToCategoryDto(it) }
        }
        log.info { "Fetched parent categories in bulk" }
        return bulkCategories
    }


    override fun getAllParentCategories(categoryCode: Int): List<CategoryDto> {
        log.info { "${"Fetching all parent categories for categoryCode: {}"} $categoryCode" }
        val categories = categoryRepository.findAllParentsByCategoryCode(categoryCode)
        val categoryDtos = categories.map { categoryMapper.categoryToCategoryDto(it) }
        log.info { "Fetched ${categoryDtos.size} parent categories for categoryCode: $categoryCode" }
        return categoryDtos
    }

    override fun getAllCategories(): List<CategoryDto> {
        log.info { "Fetching all categories" }
        val categories = categoryRepository.findAll()
        val categoryDtos = categories.map { categoryMapper.categoryToCategoryDto(it) }
        log.info { "Fetched ${categoryDtos.size} categories" }
        return categoryDtos
    }

    override fun getCategoryById(id: UUID): CategoryDto {
        log.info { "Fetching category by ID: $id" }
        val category = categoryRepository.findById(id)
                .orElseThrow {
                    log.error { "Category not found for ID: $id" }
                    NotFoundException("Category not found")
                }
        val categoryDto = categoryMapper.categoryToCategoryDto(category)
        log.info { "Fetched category: $categoryDto" }
        return categoryDto
    }

    override fun getCategoryByName(name: String): CategoryDto? {
        log.info { "Fetching category by name: $name" }

        return categoryRepository.findByName(name)?.let { category ->
            categoryMapper.categoryToCategoryDto(category).also {
                log.info { "Fetched category: $it" }
            }
        } ?: run {
            log.warn { "Category not found for name: $name" }
            throw NotFoundException("Category not found")
        }
    }

    fun createCategory(categoryDto: CategoryDto): CategoryDto {
        log.info { "Creating new category: $categoryDto" }

        val lowerCasedName = categoryDto.name?.lowercase(Locale.getDefault())
        lowerCasedName?.let {
            categoryRepository.findByName(it)?.let {
                log.error { "Category already exists for name: $lowerCasedName" }
                throw CategoryAlreadyExistsException("Category already exists")
            }
        }

        categoryDto.parentCode?.let { parentCode ->
            categoryRepository.findByCategoryCode(parentCode)
                ?: throw NotFoundException("Parent category not found")
        }

        val category = categoryMapper.categoryDtoToCategory(categoryDto)
        val savedCategory = categoryRepository.saveAndFlush(category)
        val savedCategoryDto = categoryMapper.categoryToCategoryDto(savedCategory)
        log.info { "Created new category: $savedCategoryDto" }
        publisher.publishEvent(CategoriesChangedEvent(Any()))
        return savedCategoryDto
    }

    fun updateCategory(id: UUID, categoryDto: CategoryDto): CategoryDto {
        log.info { "Updating category with ID: $id" }
        val category = categoryRepository.findById(id)
                .orElseThrow {
                    log.error { "Category not found for ID: $id" }
                    NotFoundException("Category not found")
                }

        categoryDto.parentCode?.let { parentCode ->
            categoryRepository.findByCategoryCode(parentCode)
                ?: throw NotFoundException("Parent category not found")
        }

        categoryMapper.mergeCategories(category, categoryDto)
        val savedCategory = categoryRepository.saveAndFlush(category)
        val savedCategoryDto = categoryMapper.categoryToCategoryDto(savedCategory)
        log.info { "Updated category: $savedCategoryDto" }
        publisher.publishEvent(CategoriesChangedEvent(Any()))
        return savedCategoryDto
    }

    fun activateCategory(id: UUID): CategoryDto {
        log.info { "Updating category status with ID: $id" }
        val category = categoryRepository.findById(id)
            .orElseThrow {
                log.error { "Category not found for ID: $id" }
                NotFoundException("Category not found")
            }

        category.active = category.active?.not()
        log.info { "Category status with ID: $id has been updated to: ${category.active}" }

        val savedCategory = categoryRepository.save(category)
        val savedCategoryDto = categoryMapper.categoryToCategoryDto(savedCategory)
        log.info { "Updated category: $savedCategoryDto" }
        publisher.publishEvent(CategoriesChangedEvent(Any()))
        return savedCategoryDto
    }
}