package com.masterello.category.controller

import com.masterello.category.dto.CategoryBulkRequest
import com.masterello.category.dto.CategoryDto
import com.masterello.category.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "API for managing categories")
class CategoryController {

    @Autowired
    private lateinit var categoryService: CategoryService

    /**
     * Get child categories by category code.
     * @param categoryCode the category code of the category
     * @return a list of child categories
     */
    @Operation(summary = "Get Child Categories", description = "Retrieve child categories by category code")
    @ApiResponse(responseCode = "200", description = "Returns the list of child categories")
    @GetMapping("/child")
    fun getChildCategories(@RequestParam categoryCode: Int): List<CategoryDto> {
        return categoryService.getAllChildCategories(categoryCode)
    }

    /**
     * Get child categories by category codes in bulk mode
     * @param categoryCodes the list of category codes
     * @return a map of category codes and list of child categories
     */
    @Operation(summary = "Get Child Categories Bulk", description = "Retrieve child categories by category codes")
    @ApiResponse(responseCode = "200", description = "Returns the map of child categories by codes")
    @PostMapping("/child-bulk")
    fun getChildCategoriesBulk(@RequestBody categoryCodes: CategoryBulkRequest): Map<Int,List<CategoryDto>> {
        return categoryService.getAllChildCategoriesBulk(categoryCodes)
    }

    /**
     * Get parent categories by category codes in bulk mode
     * @param categoryCodes the list of category codes
     * @return a map of category codes and list of parent categories
     */
    @Operation(summary = "Get Child Categories Bulk", description = "Retrieve parent categories by category codes")
    @ApiResponse(responseCode = "200", description = "Returns the map of parent categories by codes")
    @PostMapping("/parents-bulk")
    fun getParentCategoriesBulk(@RequestBody categoryCodes: CategoryBulkRequest): Map<Int,List<CategoryDto>> {
        return categoryService.getAllParentCategoriesBulk(categoryCodes)
    }

    /**
     * Get parent categories by category code.
     * @param categoryCode the category code of the category
     * @return a list of parent categories
     */
    @Operation(summary = "Get Parent Categories", description = "Retrieve parent categories by category code")
    @ApiResponse(responseCode = "200", description = "Returns the list of parent categories")
    @GetMapping("/parents")
    fun getParentCategories(@RequestParam categoryCode: Int): List<CategoryDto> {
        return categoryService.getAllParentCategories(categoryCode)
    }

    /**
     * Get all categories.
     * @return a list of all categories
     */
    @Operation(summary = "Get All Categories", description = "Retrieve all categories")
    @ApiResponse(responseCode = "200", description = "Returns the list of all categories")
    @GetMapping("/")
    fun getCategories(): List<CategoryDto> {
        return categoryService.getAllCategories()
    }

    /**
     * Get a category by name.
     * @param name the name of the category
     * @return the category if found, otherwise null
     */
    @Operation(summary = "Get Category by Name", description = "Retrieve a category by its name")
    @ApiResponse(responseCode = "200", description = "Returns the category")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @GetMapping("/name")
    fun getCategoryByName(@RequestParam name: String): CategoryDto? {
        return categoryService.getCategoryByName(name)
    }

    /**
     * Get a category by ID.
     * @param id the ID of the category
     * @return the category
     */
    @Operation(summary = "Get Category by ID", description = "Retrieve a category by its ID")
    @ApiResponse(responseCode = "200", description = "Returns the category")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: String): CategoryDto {
        return categoryService.getCategoryById(UUID.fromString(id))

    }

    /**
     * Create a new category.
     * @param category the category data transfer object
     * @return the created category
     */
    @Operation(summary = "Create Category", description = "Create a new category")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @PostMapping("/")
    fun createCategory(@RequestBody category: CategoryDto): CategoryDto {
        return categoryService.createCategory(category)
    }

    /**
     * Update an existing category.
     * @param id the ID of the category
     * @param category the category data transfer object
     * @return the updated category
     */
    @Operation(summary = "Update Category", description = "Update an existing category")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: UUID,
                       @RequestBody category: CategoryDto): CategoryDto {
        return categoryService.updateCategory(id, category)
    }

    /**
     * Activate an existing category.
     * @param id the ID of the category
     * @return the updated category
     */
    @Operation(summary = "Activate/Deactivate Category", description = "Activate/Deactivate an existing category")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @PutMapping("/{id}/activate")
    fun activateCategory(@PathVariable id: UUID): CategoryDto {
        return categoryService.activateCategory(id)
    }


}