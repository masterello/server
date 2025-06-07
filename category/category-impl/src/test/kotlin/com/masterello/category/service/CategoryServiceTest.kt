package com.masterello.category.service

import com.masterello.category.dto.CategoryBulkRequest
import com.masterello.category.dto.CategoryDto
import com.masterello.category.entity.Category
import com.masterello.category.exception.CategoryAlreadyExistsException
import com.masterello.category.exception.NotFoundException
import com.masterello.category.mapper.CategoryMapper
import com.masterello.category.repository.CategoryRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var publisher: ApplicationEventPublisher

    @Mock
    private lateinit var categoryMapper: CategoryMapper

    @InjectMocks
    private lateinit var categoryService: CategoryService

    private lateinit var categoryId: UUID
    private lateinit var category: Category
    private lateinit var categoryDto: CategoryDto

    @BeforeEach
    fun setUp() {
        categoryId = UUID.randomUUID()
        category = Category(
            uuid = categoryId,
            name = "Test Category",
            originalName = "Original Test Category",
            description = "This is a test category",
            parentCode = 123,
            isService = true,
            active = true
        )
        categoryDto = CategoryDto(
            uuid = categoryId,
            name = "Test Category DTO",
            description = "This is a test category DTO",
            categoryCode = 123,
            parentCode = 123,
            isService = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            active = false
        )
    }

    @Test
    fun `activateCategory should activate category`() {
        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        `when`(categoryRepository.save(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(categoryDto)

        val result = categoryService.activateCategory(categoryId)

        assertNotNull(result)
        assertEquals(categoryId, result.uuid)
        assertFalse(result.active?:false)

        verify(categoryRepository).findById(categoryId)
        verify(categoryRepository).save(any(Category::class.java))
        verify(categoryMapper).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `activateCategory should throw NotFoundException when category not found`() {
        `when`(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.empty())

        assertThrows<NotFoundException> {
            categoryService.activateCategory(categoryId)
        }
    }

    @Test
    fun `activateCategory should correctly convert Category to CategoryDto`() {
        val updatedCategory = category.copy(active = false)
        val updatedCategoryDto = categoryDto.copy(active = false)

        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        `when`(categoryRepository.save(any(Category::class.java))).thenReturn(updatedCategory)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(updatedCategoryDto)

        val result = categoryService.activateCategory(categoryId)

        assertNotNull(result)
        assertEquals(categoryId, result.uuid)
        assertFalse(result.active?:false)

        verify(categoryRepository).findById(categoryId)
        verify(categoryRepository).save(any(Category::class.java))
        verify(categoryMapper).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `updateCategory should successfully update and return updated CategoryDto`() {
        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        `when`(categoryRepository.findByCategoryCode(categoryDto.parentCode!!)).thenReturn(category)
        `when`(categoryRepository.saveAndFlush(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(categoryDto)

        val result = categoryService.updateCategory(categoryId, categoryDto)

        assertNotNull(result)
        assertEquals(categoryDto, result)
    }

    @Test
    fun `updateCategory should throw NotFoundException when category is not found`() {
        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.empty())

        assertThrows<NotFoundException> {
            categoryService.updateCategory(categoryId, categoryDto)
        }

        verify(categoryRepository).findById(categoryId)
    }

    @Test
    fun `updateCategory should throw NotFoundException when parent category is not found`() {
        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        `when`(categoryRepository.findByCategoryCode(categoryDto.parentCode!!)).thenReturn(null)

        assertThrows<NotFoundException> {
            categoryService.updateCategory(categoryId, categoryDto)
        }

        verify(categoryRepository).findById(categoryId)
        verify(categoryRepository).findByCategoryCode(categoryDto.parentCode!!)
    }

    @Test
    fun `updateCategory should call mergeCategories to update the entity`() {
        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        `when`(categoryRepository.findByCategoryCode(categoryDto.parentCode!!)).thenReturn(category)
        `when`(categoryRepository.saveAndFlush(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(categoryDto)

        val result = categoryService.updateCategory(categoryId, categoryDto)

        assertNotNull(result)
        verify(categoryMapper).mergeCategories(category, categoryDto)
    }

    @Test
    fun `updateCategory should skip parent category check if parentCode is not provided`() {
        val categoryDtoWithoutParentCode = categoryDto.copy(parentCode = null)

        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))
        `when`(categoryRepository.saveAndFlush(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(categoryDtoWithoutParentCode)

        val result = categoryService.updateCategory(categoryId, categoryDtoWithoutParentCode)

        assertNotNull(result)
        assertEquals(categoryDtoWithoutParentCode, result)

        verify(categoryRepository).findById(categoryId)
        verify(categoryRepository).saveAndFlush(any(Category::class.java))
        verify(categoryMapper).categoryToCategoryDto(any(Category::class.java))
        verify(categoryRepository, never()).findByCategoryCode(anyInt())
    }

    @Test
    fun `createCategory should successfully create and return CategoryDto`() {
        `when`(categoryRepository.findByName("test category dto")).thenReturn(null)
        `when`(categoryRepository.findByCategoryCode(categoryDto.parentCode!!)).thenReturn(category)
        `when`(categoryRepository.saveAndFlush(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(categoryDto)
        `when`(categoryMapper.categoryDtoToCategory(any(CategoryDto::class.java))).thenReturn(category)

        val result = categoryService.createCategory(categoryDto)

        assertNotNull(result)
        assertEquals(categoryDto, result)

        verify(categoryRepository).findByName("test category dto")
        verify(categoryRepository).saveAndFlush(any(Category::class.java))
        verify(categoryMapper).categoryDtoToCategory(any(CategoryDto::class.java))
        verify(categoryMapper).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `createCategory should throw CategoryAlreadyExistsException when category name already exists`() {
        `when`(categoryRepository.findByName("test category dto")).thenReturn(category)

        assertThrows<CategoryAlreadyExistsException> {
            categoryService.createCategory(categoryDto)
        }

        verify(categoryRepository).findByName("test category dto")
        verify(categoryRepository, never()).saveAndFlush(any(Category::class.java))
        verify(categoryMapper, never()).categoryDtoToCategory(any(CategoryDto::class.java))
        verify(categoryMapper, never()).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `createCategory should throw NotFoundException when parent category is not found`() {
        `when`(categoryRepository.findByName("test category dto")).thenReturn(null)
        `when`(categoryRepository.findByCategoryCode(categoryDto.parentCode!!)).thenReturn(null)

        assertThrows<NotFoundException> {
            categoryService.createCategory(categoryDto)
        }

        verify(categoryRepository).findByName("test category dto")
        verify(categoryRepository).findByCategoryCode(categoryDto.parentCode!!)
        verify(categoryRepository, never()).saveAndFlush(any(Category::class.java))
        verify(categoryMapper, never()).categoryDtoToCategory(any(CategoryDto::class.java))
        verify(categoryMapper, never()).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `createCategory should skip parent category check if parentCode is not provided`() {
        val categoryDtoWithoutParentCode = categoryDto.copy(parentCode = null)

        `when`(categoryRepository.findByName("test category dto")).thenReturn(null)
        `when`(categoryRepository.saveAndFlush(any(Category::class.java))).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(any(Category::class.java))).thenReturn(categoryDtoWithoutParentCode)
        `when`(categoryMapper.categoryDtoToCategory(any(CategoryDto::class.java))).thenReturn(category)

        val result = categoryService.createCategory(categoryDtoWithoutParentCode)

        assertNotNull(result)
        assertEquals(categoryDtoWithoutParentCode, result)

        verify(categoryRepository).findByName("test category dto")
        verify(categoryRepository).saveAndFlush(any(Category::class.java))
        verify(categoryMapper).categoryDtoToCategory(any(CategoryDto::class.java))
        verify(categoryMapper).categoryToCategoryDto(any(Category::class.java))
        verify(categoryRepository, never()).findByCategoryCode(anyInt())
    }

    @Test
    fun `getCategoryByName should successfully retrieve and return CategoryDto`() {
        `when`(categoryRepository.findByName("test category")).thenReturn(category)
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getCategoryByName("test category")

        assertNotNull(result)
        assertEquals(categoryDto, result)

        verify(categoryRepository).findByName("test category")
        verify(categoryMapper).categoryToCategoryDto(category)
    }

    @Test
    fun `getCategoryByName should throw NotFoundException when category is not found`() {
        `when`(categoryRepository.findByName("test category")).thenReturn(null)

        assertThrows<NotFoundException> {
            categoryService.getCategoryByName("test category")
        }

        verify(categoryRepository).findByName("test category")
        verify(categoryMapper, never()).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `getAllCategories should return a list of CategoryDto`() {
        val categories = listOf(category)
        val categoryDtos = listOf(categoryDto)

        `when`(categoryRepository.findAll()).thenReturn(categories)
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getAllCategories()

        assertNotNull(result)
        assertEquals(categoryDtos, result)

        verify(categoryRepository).findAll()
        verify(categoryMapper).categoryToCategoryDto(category)
    }

    @Test
    fun `getCategoryById should return CategoryDto when category is found`() {
        val categoryId = category.uuid
        `when`(categoryRepository.findById(categoryId!!)).thenReturn(Optional.of(category))
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getCategoryById(categoryId)

        assertNotNull(result)
        assertEquals(categoryDto, result)

        verify(categoryRepository).findById(categoryId)
        verify(categoryMapper).categoryToCategoryDto(category)
    }

    @Test
    fun `getCategoryById should throw NotFoundException when category is not found`() {
        val categoryId = UUID.randomUUID()
        `when`(categoryRepository.findById(categoryId)).thenReturn(Optional.empty())

        assertThrows<NotFoundException> {
            categoryService.getCategoryById(categoryId)
        }

        verify(categoryRepository).findById(categoryId)
        verify(categoryMapper, never()).categoryToCategoryDto(any(Category::class.java))
    }

    @Test
    fun `getAllChildCategories should return a list of CategoryDto`() {
        val categoryCode = 123
        val categories = listOf(category)
        val categoryDtos = listOf(categoryDto)

        `when`(categoryRepository.findAllChildsByCategoryCode(categoryCode)).thenReturn(categories)
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getAllChildCategories(categoryCode)

        assertNotNull(result)
        assertEquals(categoryDtos, result)

        verify(categoryRepository).findAllChildsByCategoryCode(categoryCode)
        verify(categoryMapper).categoryToCategoryDto(category)
    }

    @Test
    fun `getAllChildCategoriesBulk should return a map of category codes to lists of CategoryDto`() {
        val categoryRequest = CategoryBulkRequest(listOf(123, 456), serviceOnly = false)
        val categories123 = listOf(category)
        val categories456 = listOf(category)
        val categoryDto123 = listOf(categoryDto)
        val categoryDto456 = listOf(categoryDto)

        `when`(categoryRepository.findAllChildsByCategoryCode(123)).thenReturn(categories123)
        `when`(categoryRepository.findAllChildsByCategoryCode(456)).thenReturn(categories456)
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getAllChildCategoriesBulk(categoryRequest)

        assertNotNull(result)
        assertEquals(mapOf(123 to categoryDto123, 456 to categoryDto456), result)

        verify(categoryRepository).findAllChildsByCategoryCode(123)
        verify(categoryRepository).findAllChildsByCategoryCode(456)
        verify(categoryMapper, times(2)).categoryToCategoryDto(category)
    }

    @Test
    fun `getAllParentCategoriesBulk should return a map of category codes to lists of CategoryDto`() {
        val categoryRequest = CategoryBulkRequest(listOf(123, 456), serviceOnly = true)
        val categories123 = listOf(category)
        val categories456 = listOf(category)
        val categoryDto123 = listOf(categoryDto)
        val categoryDto456 = listOf(categoryDto)

        `when`(categoryRepository.findAllServiceParentsByCategoryCode(123)).thenReturn(categories123)
        `when`(categoryRepository.findAllServiceParentsByCategoryCode(456)).thenReturn(categories456)
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getAllParentCategoriesBulk(categoryRequest)

        assertNotNull(result)
        assertEquals(mapOf(123 to categoryDto123, 456 to categoryDto456), result)

        verify(categoryRepository).findAllServiceParentsByCategoryCode(123)
        verify(categoryRepository).findAllServiceParentsByCategoryCode(456)
        verify(categoryMapper, times(2)).categoryToCategoryDto(category)
    }

    @Test
    fun `getAllParentCategories should return a list of CategoryDto`() {
        val categoryCode = 123
        val categories = listOf(category)
        val categoryDtos = listOf(categoryDto)

        `when`(categoryRepository.findAllParentsByCategoryCode(categoryCode)).thenReturn(categories)
        `when`(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto)

        val result = categoryService.getAllParentCategories(categoryCode)

        assertNotNull(result)
        assertEquals(categoryDtos, result)

        verify(categoryRepository).findAllParentsByCategoryCode(categoryCode)
        verify(categoryMapper).categoryToCategoryDto(category)
    }

}