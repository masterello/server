package com.masterello.file.converter

import com.masterello.file.dto.FileType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileTypeConverterTest {
    private val converter = FileTypeConverter()

    @Test
    fun `test convertToDatabaseColumn with valid FileType`() {
        val fileType = FileType.AVATAR
        val result = converter.convertToDatabaseColumn(fileType)

        assertEquals(fileType.code, result)
    }

    @Test
    fun `test convertToDatabaseColumn with null FileType`() {
        val result = converter.convertToDatabaseColumn(null)

        assertNull(result)
    }

    @Test
    fun `test convertToEntityAttribute with valid code`() {
        val code = FileType.AVATAR.code
        val result = converter.convertToEntityAttribute(code)

        assertEquals(FileType.AVATAR, result)
    }

    @Test
    fun `test convertToEntityAttribute with null code`() {
        val result = converter.convertToEntityAttribute(null)

        assertNull(result)
    }

    @Test
    fun `test convertToEntityAttribute with invalid code`() {
        val invalidCode = -1
        val result = converter.convertToEntityAttribute(invalidCode)

        assertNull(result)
    }
}