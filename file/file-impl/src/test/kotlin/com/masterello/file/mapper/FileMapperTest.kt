package com.masterello.file.mapper

import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileStatus
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.*

class FileMapperTest {
    private val fileMapper = FileMapper()

    @Test
    fun `test mapFileDtoToFile with valid FileDto`() {
        val uuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val dto = FileDto(
            uuid = uuid,
            userUuid = userUuid,
            fileType = FileType.AVATAR,
            fileName = "testfile.webp",
            isPublic = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            fileStatus = FileStatus.UPLOADING,
            taskUuid = null
        )
        val type = FileType.AVATAR
        val status = FileStatus.UPLOADING

        val result = fileMapper.mapFileDtoToFile(dto, type, status)

        assertEquals(userUuid, result.userUuid)
        assertEquals(FileType.AVATAR, result.fileType)
        assertEquals("testfile.webp", result.fileName)
        assertTrue(result.isPublic)
    }

    @Test
    fun `test mapFileDtoToFile with null FileDto`() {
        val exception = assertThrows<IllegalArgumentException> {
            fileMapper.mapFileDtoToFile(null, FileType.AVATAR, FileStatus.UPLOADING)
        }
        assertEquals("FileDto cannot be null", exception.message)
    }

    @Test
    fun `test mapFileToDto with valid File`() {
        val uuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val file = File(
            uuid = uuid,
            userUuid = userUuid,
            fileType = FileType.AVATAR,
            fileName = "testfile.jpg",
            isPublic = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val result = fileMapper.mapFileToDto(file)

        assertEquals(uuid, result.uuid)
        assertEquals(userUuid, result.userUuid)
        assertEquals(FileType.AVATAR, result.fileType)
        assertEquals("testfile.jpg", result.fileName)
        assertTrue(result.isPublic)
        assertNotNull(result.createdDate)
        assertNotNull(result.updatedDate)
    }
}