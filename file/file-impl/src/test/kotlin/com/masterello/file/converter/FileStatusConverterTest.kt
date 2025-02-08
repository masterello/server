package com.masterello.file.converter

import com.masterello.file.dto.FileStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileStatusConverterTest {
    private val converter = FileStatusConverter()

    @Test
    fun `test convertToDatabaseColumn with valid FileStatus`() {
        val fileStatus = FileStatus.UPLOADING
        val result = converter.convertToDatabaseColumn(fileStatus)

        assertEquals(fileStatus.code, result)
    }

    @Test
    fun `test convertToDatabaseColumn with null FileStatus`() {
        val result = converter.convertToDatabaseColumn(null)

        assertNull(result)
    }

    @Test
    fun `test convertToEntityAttribute with valid code`() {
        val code = FileStatus.UPLOADED.code
        val result = converter.convertToEntityAttribute(code)

        assertEquals(FileStatus.UPLOADED, result)
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