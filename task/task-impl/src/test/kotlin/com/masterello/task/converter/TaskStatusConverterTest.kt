package com.masterello.task.converter

import com.masterello.task.dto.TaskStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TaskStatusConverterTest {

    private val converter = TaskStatusConverter()

    @Test
    fun `convertToDatabaseColumn should return code for valid TaskStatus`() {
        val taskStatus = TaskStatus.IN_PROGRESS
        val result = converter.convertToDatabaseColumn(taskStatus)
        assertEquals(taskStatus.code, result)
    }

    @Test
    fun `convertToDatabaseColumn should return null for null TaskStatus`() {
        val result = converter.convertToDatabaseColumn(null)
        assertNull(result)
    }

    @Test
    fun `convertToEntityAttribute should return TaskStatus for valid code`() {
        val validCode = TaskStatus.DONE.code
        val result = converter.convertToEntityAttribute(validCode)
        assertEquals(TaskStatus.DONE, result)
    }

    @Test
    fun `convertToEntityAttribute should return null for null code`() {
        val result = converter.convertToEntityAttribute(null)
        assertNull(result)
    }

    @Test
    fun `convertToEntityAttribute should return null for invalid code`() {
        val invalidCode = -99
        val result = converter.convertToEntityAttribute(invalidCode)
        assertNull(result)
    }
}
