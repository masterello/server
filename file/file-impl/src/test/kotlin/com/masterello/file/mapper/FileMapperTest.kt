package com.masterello.file.mapper

import com.masterello.file.dto.FileDto
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
            fileName = "testfile",
            isPublic = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            file = null
        )
        val type = FileType.AVATAR
        val fileName = "testfile"
        val fileExtension = "jpg"

        val result = fileMapper.mapFileDtoToFile(dto, type, fileName, fileExtension)

        assertEquals(userUuid, result.userUuid)
        assertEquals(FileType.AVATAR, result.fileType)
        assertEquals("testfile", result.fileName)
        assertTrue(result.isPublic)
        assertEquals("jpg", result.fileExtension)
    }

    @Test
    fun `test mapFileDtoToFile with null FileDto`() {
        val exception = assertThrows<IllegalArgumentException> {
            fileMapper.mapFileDtoToFile(null, FileType.AVATAR, "testfile", "jpg")
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
            fileName = "testfile",
            isPublic = true,
            fileExtension = "jpg",
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val result = fileMapper.mapFileToDto(file)

        assertEquals(uuid, result.uuid)
        assertEquals(userUuid, result.userUuid)
        assertEquals(FileType.AVATAR, result.fileType)
        assertEquals("testfile", result.fileName)
        assertTrue(result.isPublic)
        assertNotNull(result.createdDate)
        assertNotNull(result.updatedDate)
    }

    @Test
    fun `test mapAvatarThumbnailToFile`() {
        val uuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val file = FileDto(
            uuid = uuid,
            userUuid = userUuid,
            fileType = FileType.AVATAR,
            fileName = "testfile",
            isPublic = true,
            file = null,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val result = fileMapper.mapAvatarThumbnailToFile(file, FileType.AVATAR, "testfile", "webp",
            112, true)

        assertEquals(userUuid, result.userUuid)
        assertEquals(FileType.AVATAR, result.fileType)
        assertEquals("testfile", result.fileName)
        assertTrue(result.isPublic)
        assertTrue(result.avatarThumbnail!!)
        assertEquals(112, result.thumbailSize)
    }
}