package com.masterello.file.service

import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileStatus
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import com.masterello.file.exception.FileTypeException
import com.masterello.file.exception.NotFoundException
import com.masterello.file.mapper.FileMapper
import com.masterello.file.repository.FileRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class FileServiceTest {
    @Mock
    private lateinit var fileRepository: FileRepository

    @Mock
    private lateinit var fileMapper: FileMapper

    @Mock
    private lateinit var storageService: StorageService

    @InjectMocks
    private lateinit var fileService: FileService

    @Test
    fun `test findAllFilesByUserUuid returns list of file DTOs`() {
        val userUuid = UUID.randomUUID()
        val file = File(
            uuid = UUID.randomUUID(),
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.DOCUMENT,
            isPublic = false,
        )

        val fileDto = FileDto(
            uuid = file.uuid,
            userUuid = file.userUuid,
            fileType = file.fileType,
            fileName = file.fileName,
            isPublic = file.isPublic,
            fileStatus = file.fileStatus,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            taskUuid = UUID.randomUUID()
        )

        `when`(fileRepository.findAllFilesByUserUuid(userUuid)).thenReturn(listOf(file))
        `when`(fileMapper.mapFileToDto(file)).thenReturn(fileDto)

        val result = fileService.findAllFilesByUserUuid(userUuid)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(fileDto, result[0])
    }

    @Test
    fun `test findImagesBulk with documents`() {
        assertThrows<FileTypeException> {
            fileService.findImagesBulk(FileType.DOCUMENT, listOf(UUID.randomUUID()))
        }
    }

    @Test
    fun `test findImagesBulk with avatars no data`() {
        val userUuid = UUID.randomUUID()

        `when`(fileRepository.findAllImagesByUserUuidsAndType(FileType.AVATAR.code, listOf(userUuid))).thenReturn(listOf())
        val result = fileService.findImagesBulk(FileType.AVATAR, listOf(userUuid))

        assertEquals(0, result.size)
    }

    @Test
    fun `test findImagesBulk with avatars`() {
        val userUuid = UUID.randomUUID()
        val userUuid2 = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()

        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.AVATAR,
            isPublic = false
        )

        `when`(fileRepository.findAllImagesByUserUuidsAndType(FileType.AVATAR.code, listOf(userUuid, userUuid2))).thenReturn(listOf(file))
        val result = fileService.findImagesBulk(FileType.AVATAR, listOf(userUuid, userUuid2))

        assertEquals(1, result.size)
        assertEquals(userUuid, result[0].userUUID)
        assertEquals(fileUuid, result[0].imageDtos[0])
    }

    @Test
    fun `test findImagesBulk with portfolios`() {
        val userUuid = UUID.randomUUID()
        val userUuid2 = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()

        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.PORTFOLIO,
            isPublic = false,
        )

        `when`(fileRepository.findAllImagesByUserUuidsAndType(FileType.PORTFOLIO.code, listOf(userUuid, userUuid2))).thenReturn(listOf(file))
        val result = fileService.findImagesBulk(FileType.PORTFOLIO, listOf(userUuid, userUuid2))

        assertEquals(1, result.size)
        assertEquals(userUuid, result[0].userUUID)
        assertEquals(fileUuid, result[0].imageDtos[0])
    }

    @Test
    fun `test findAllImagesByUserUuid returns list of file DTOs`() {
        val userUuid = UUID.randomUUID()
        val file = File(
            uuid = UUID.randomUUID(),
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.AVATAR,
            isPublic = false,
        )

        val fileDto = FileDto(
            uuid = file.uuid,
            userUuid = file.userUuid,
            fileType = file.fileType,
            fileName = file.fileName,
            isPublic = file.isPublic,
            fileStatus = file.fileStatus,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            taskUuid = null
        )

        `when`(fileRepository.findAllImagesByUserUuid(userUuid)).thenReturn(listOf(file))
        `when`(fileMapper.mapFileToDto(file)).thenReturn(fileDto)

        val result = fileService.findAllImagesByUserUuid(userUuid)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(fileDto, result[0])
    }

    @Test
    fun `test storeFile uploads document file correctly`() {
        val payload = FileDto(
            fileName = "test.txt",
            fileType = FileType.DOCUMENT,
            userUuid = UUID.randomUUID(),
            fileStatus = FileStatus.UPLOADING,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            taskUuid = UUID.randomUUID()
        )

        val entity = File(
            uuid = UUID.randomUUID(),
            userUuid = payload.userUuid,
            fileName = payload.fileName,
            fileType = payload.fileType,
            isPublic = false,
            taskUuid = null
        )

        `when`(fileMapper.mapFileDtoToFile(payload, payload.fileType, FileStatus.UPLOADING)).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(listOf(payload))

        verify(fileRepository).save(entity)
    }

    @Test
    fun `test storeFile uploads multiple documents file correctly`() {
        val payload = FileDto(
            fileName = "test.txt",
            fileType = FileType.DOCUMENT,
            userUuid = UUID.randomUUID(),
            fileStatus = FileStatus.UPLOADING,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            taskUuid = UUID.randomUUID()
        )

        val entity = File(
            uuid = UUID.randomUUID(),
            userUuid = payload.userUuid,
            fileName = payload.fileName,
            fileType = payload.fileType,
            isPublic = false,
            taskUuid = null
        )

        `when`(fileMapper.mapFileDtoToFile(payload, payload.fileType, FileStatus.UPLOADING)).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(listOf(payload, payload, payload))

        verify(fileRepository, times(3)).save(entity)
    }


    @Test
    fun `test storeFile uploads image file correctly and removing old avatar`() {
        val userUuid = UUID.randomUUID()
        val payload = FileDto(
            fileName = "test.jpg",
            fileType = FileType.AVATAR,
            userUuid = userUuid,
            taskUuid = null,
            fileStatus = FileStatus.UPLOADING,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val fileUuid = UUID.randomUUID()

        val entity = File(
            uuid = fileUuid,
            userUuid = payload.userUuid,
            fileName = payload.fileName,
            fileType = payload.fileType,
            isPublic = false
        )

        `when`(fileRepository.getAvatarByUserUuid(userUuid)).thenReturn(Optional.of(entity))
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.AVATAR, FileStatus.UPLOADING)).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(listOf(payload))

        verify(fileRepository, times(1)).delete(any())
        verify(fileRepository, times(1)).save(any())
        verify(storageService, times(1)).removeFolder(entity)
    }


    @Test
    fun `test removeUserFile no file found`() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()

        assertThrows<NotFoundException> { fileService.removeFile(userUuid, fileUuid) }
    }

    @Test
    fun `test removeUserFile returns file deleted response`() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val expectedFileName = "test.txt"
        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.DOCUMENT,
            isPublic = false,
        )

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file)
        `when`(storageService.removeFolder(file)).thenReturn(true)

        val result = fileService.removeFile(userUuid, fileUuid)

        assertTrue(result)
        verify(fileRepository).findFileByUuidAndUserUuid(fileUuid, userUuid)
        verify(fileRepository).delete(file)
    }

    @Test
    fun `test removeUserFile returns file not deleted response`() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val expectedFileName = "test.txt"

        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.DOCUMENT,
            isPublic = false,
        )

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file)
        `when`(storageService.removeFolder(file)).thenReturn(false)

        val result = fileService.removeFile(userUuid, fileUuid)

        assertFalse(result)
        verify(fileRepository).findFileByUuidAndUserUuid(fileUuid, userUuid)
        verify(storageService).removeFolder(file)
    }

    @Test
    fun `markAsUploaded should update file statuses and return DTOs`() {
        val userUuid = UUID.randomUUID()
        val fileUuid1 = UUID.randomUUID()
        val fileUuid2 = UUID.randomUUID()
        val filesToUpdate = listOf(fileUuid1, fileUuid2)

        val file1 = File(
            uuid = fileUuid1,
            userUuid = userUuid,
            fileName = "test.png",
            fileType = FileType.DOCUMENT,
            isPublic = false,
        )
        val file2 = File(
            uuid = fileUuid2,
            userUuid = userUuid,
            fileName = "test2.png",
            fileType = FileType.DOCUMENT,
            isPublic = false,
        )
        val foundFiles = listOf(file1, file2)

        val fileDto1 = FileDto(
            fileName = "test.jpg",
            fileType = FileType.AVATAR,
            userUuid = userUuid,
            taskUuid = null,
            fileStatus = FileStatus.UPLOADING,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )
        val fileDto2 = FileDto(
            fileName = "test.jpg",
            fileType = FileType.AVATAR,
            userUuid = userUuid,
            taskUuid = null,
            fileStatus = FileStatus.UPLOADING,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )
        `when`(fileRepository.findAllFilesByIdsAndUserUuid(userUuid, filesToUpdate)).thenReturn(foundFiles)
        `when`(fileRepository.saveAll(listOf(file1, file2))).thenReturn(foundFiles)
        `when`(fileMapper.mapFileToDto(file1)).thenReturn(fileDto1)
        `when`(fileMapper.mapFileToDto(file2)).thenReturn(fileDto2)

        val result = fileService.markAsUploaded(userUuid, filesToUpdate)

        assertEquals(2, result.size)
        assertEquals(FileStatus.UPLOADED, file1.fileStatus)
        assertEquals(FileStatus.UPLOADED, file2.fileStatus)
        verify(fileRepository).saveAll(foundFiles)
    }

    @Test
    fun `markAsUploaded should throw NotFoundException when some files are missing`() {
        val userUuid = UUID.randomUUID()
        val fileUuid1 = UUID.randomUUID()
        val fileUuid2 = UUID.randomUUID()
        val filesToUpdate = listOf(fileUuid1, fileUuid2)

        val file1 = File(
            uuid = fileUuid1,
            userUuid = userUuid,
            fileName = "test.png",
            fileType = FileType.DOCUMENT,
            isPublic = false,
        )
        val foundFiles = listOf(file1)

        `when`(fileRepository.findAllFilesByIdsAndUserUuid(userUuid, filesToUpdate)).thenReturn(foundFiles)

        assertThrows<NotFoundException> {
            fileService.markAsUploaded(userUuid, filesToUpdate)
        }
    }
}