package com.masterello.file.util

import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileType
import com.masterello.file.exception.FileNameException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import java.util.*

class FileUtilTest {

    @Test
    fun `test prepareHeaders`() {
        val filename = "test.txt"
        val headers: HttpHeaders = FileUtil.prepareHeaders(filename)

        assertEquals(MediaType.TEXT_PLAIN_VALUE, headers.contentType?.toString())
        assertEquals("inline; filename=test.txt", headers["Content-Disposition"]?.get(0))
    }

    @Test
    fun `test prepareHeaders with unknown file type`() {
        val filename = "unknownfile.unknown"
        val headers: HttpHeaders = FileUtil.prepareHeaders(filename)

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, headers.contentType?.toString())
        assertEquals("inline; filename=unknownfile.unknown", headers["Content-Disposition"]?.get(0))
    }

    @Test
    fun `test getFileNameWithoutExtension`() {
        val filename = "example.txt"
        val result = FileUtil.getFileNameWithoutExtension(filename)

        assertEquals("example", result)
    }

    @Test
    fun `test getFileNameWithoutExtension without extension`() {
        val filename = "example"
        val result = FileUtil.getFileNameWithoutExtension(filename)

        assertEquals("example", result)
    }

    @Test
    fun `test getFileExtension`() {
        val filename = "example.txt"
        val result = FileUtil.getFileExtension(filename)

        assertEquals("txt", result)
    }

    @Test
    fun `test getFileExtension without extension`() {
        val filename = "example"
        val result = FileUtil.getFileExtension(filename)

        assertEquals("", result)
    }

    @Test
    fun `test getFileName with FileDto containing fileName`() {
        val fileDto = FileDto(uuid = UUID.randomUUID(), userUuid = UUID.randomUUID(),
            fileName = "testfile.txt", isPublic = true, fileType = FileType.AVATAR, createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(), file = null)
        val result = FileUtil.getFileName(fileDto)

        assertEquals("testfile.txt", result)
    }

    @Test
    fun `test getFileName with FileDto containing MultipartFile`() {
        val multipartFile: MultipartFile = mock()
        `when`(multipartFile.originalFilename).thenReturn("multipartfile.txt")
        val fileDto = FileDto(uuid = UUID.randomUUID(), userUuid = UUID.randomUUID(),
            fileName = null, isPublic = true, fileType = FileType.AVATAR, createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(), file = multipartFile)
        val result = FileUtil.getFileName(fileDto)

        assertEquals("multipartfile.txt", result)
    }

    @Test
    fun `test getFileName throws exception`() {
        val fileDto = FileDto(uuid = UUID.randomUUID(), userUuid = UUID.randomUUID(),
            fileName = null, isPublic = true, fileType = FileType.AVATAR, createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(), file = null)

        val exception = assertThrows<FileNameException> {
            FileUtil.getFileName(fileDto)
        }

        assertEquals("File name is not provided", exception.message)
    }
}