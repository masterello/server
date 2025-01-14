package com.masterello.task.converter

import com.masterello.task.dto.ReviewerType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TaskReviewerTypeConverterTest {

    private val converter = TaskReviewerTypeConverter()

    @Test
    fun `convertToDatabaseColumn should return code for valid ReviewerType`() {
        val reviewerType = ReviewerType.WORKER
        val result = converter.convertToDatabaseColumn(reviewerType)
        assertEquals(reviewerType.code, result)
    }

    @Test
    fun `convertToDatabaseColumn should return null for null ReviewerType`() {
        val result = converter.convertToDatabaseColumn(null)
        assertNull(result)
    }

    @Test
    fun `convertToEntityAttribute should return ReviewerType for valid code`() {
        val validCode = ReviewerType.USER.code
        val result = converter.convertToEntityAttribute(validCode)
        assertEquals(ReviewerType.USER, result)
    }

    @Test
    fun `convertToEntityAttribute should return null for null code`() {
        val result = converter.convertToEntityAttribute(null)
        assertNull(result)
    }

    @Test
    fun `convertToEntityAttribute should return null for invalid code`() {
        val invalidCode = -1
        val result = converter.convertToEntityAttribute(invalidCode)
        assertNull(result)
    }
}