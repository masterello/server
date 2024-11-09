package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import com.masterello.file.exception.FileDimensionException
import com.masterello.file.exception.FileNotProvidedException
import com.masterello.file.exception.NotFoundException
import com.masterello.file.mapper.FileMapper
import com.masterello.file.repository.FileRepository
import com.masterello.file.util.FileUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class FileServiceTest {
    @Mock
    private lateinit var fileRepository: FileRepository

    @Mock
    private lateinit var fileMapper: FileMapper

    @Mock
    private lateinit var imageService: ImageService

    @Mock
    private lateinit var fileProperties: FileProperties

    @Mock
    private lateinit var storageService: StorageService

    @InjectMocks
    private lateinit var fileService: FileService

    @Test
    fun `test downloadUserFile returns file content`() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val expectedFileName = "test.txt"
        val expectedContent = ByteArray(10)

        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.DOCUMENT,
            isPublic = false,
            fileExtension = "txt"
        )

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file)
        `when`(storageService.downloadFile(file)).thenReturn(expectedContent)

        val result = fileService.downloadUserFile(userUuid, fileUuid)

        assertNotNull(result)
        assertEquals(expectedFileName, result?.first)
        assertArrayEquals(expectedContent, result?.second)
    }

    @Test
    fun `test findAllFilesByUserUuid returns list of file DTOs`() {
        val userUuid = UUID.randomUUID()
        val file = File(
            uuid = UUID.randomUUID(),
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.DOCUMENT,
            isPublic = false,
            fileExtension = "txt"
        )

        val fileDto = FileDto(
            uuid = file.uuid,
            userUuid = file.userUuid,
            fileType = file.fileType,
            fileName = file.fileName,
            isPublic = file.isPublic,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            file = null
        )

        `when`(fileRepository.findAllFilesByUserUuid(userUuid)).thenReturn(listOf(file))
        `when`(fileMapper.mapFileToDto(file)).thenReturn(fileDto)

        val result = fileService.findAllFilesByUserUuid(userUuid)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(fileDto, result[0])
    }

    @Test
    fun `test findAllImagesByUserUuid returns list of file DTOs`() {
        val userUuid = UUID.randomUUID()
        val file = File(
            uuid = UUID.randomUUID(),
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.IMAGE,
            isPublic = false,
            fileExtension = "txt"
        )

        val fileDto = FileDto(
            uuid = file.uuid,
            userUuid = file.userUuid,
            fileType = file.fileType,
            fileName = file.fileName,
            isPublic = file.isPublic,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            file = null
        )

        `when`(fileRepository.findAllImagesByUserUuid(userUuid)).thenReturn(listOf(file))
        `when`(fileMapper.mapFileToDto(file)).thenReturn(fileDto)

        val result = fileService.findAllImagesByUserUuid(userUuid)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(fileDto, result[0])
    }

    @Test
    fun `test findAllThumbnailsByUserUuid returns list of file DTOs`() {
        val userUuid = UUID.randomUUID()
        val file = File(
            uuid = UUID.randomUUID(),
            userUuid = userUuid,
            fileName = "test.txt",
            fileType = FileType.THUMBNAIL,
            isPublic = false,
            fileExtension = "txt"
        )

        val fileDto = FileDto(
            uuid = file.uuid,
            userUuid = file.userUuid,
            fileType = file.fileType,
            fileName = file.fileName,
            isPublic = file.isPublic,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            file = null
        )

        `when`(fileRepository.findAllThumbnailsByUserUuid(userUuid)).thenReturn(listOf(file))
        `when`(fileMapper.mapFileToDto(file)).thenReturn(fileDto)

        val result = fileService.findAllThumbnailsByUserUuid(userUuid)

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(fileDto, result[0])
    }

    @Test
    fun `test storeFile throws FileNotProvidedException`() {
        val dto = FileDto(
            uuid = UUID.randomUUID(),
            userUuid = UUID.randomUUID(),
            fileType = FileType.IMAGE,
            fileName = "testfile",
            isPublic = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            file = null
        )

        assertThrows<FileNotProvidedException> {
            fileService.storeFile(dto)
        }
    }

    @Test
    fun `test storeFile uploads document file correctly`() {
        val payload = FileDto(
            file = mock(),
            fileName = "test.txt",
            fileType = FileType.DOCUMENT,
            userUuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val entity = File(
            uuid = UUID.randomUUID(),
            userUuid = payload.userUuid,
            fileName = payload.fileName!!,
            fileType = payload.fileType,
            isPublic = false,
            fileExtension = FileUtil.getFileExtension(payload.fileName!!)
        )

        `when`(fileMapper.mapFileDtoToFile(payload, payload.fileType, payload.fileName!!,
            "txt")).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(payload)

        verify(storageService).uploadFile(entity, payload.file)
    }

    @Test
    fun `test storeFile uploads image file correctly`() {
        val payload = FileDto(
            file = mock(),
            fileName = "test.jpg",
            fileType = FileType.IMAGE,
            userUuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val entity = File(
            uuid = UUID.randomUUID(),
            userUuid = payload.userUuid,
            fileName = payload.fileName!!,
            fileType = payload.fileType,
            isPublic = false,
            fileExtension = FileUtil.getFileExtension(payload.fileName!!)
        )
        val baos = ByteArrayOutputStream()
        val bufferedImage = BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB)
        `when`(imageService.createBufferedImage(any())).thenReturn(bufferedImage)
        `when`(imageService.compressedImage(bufferedImage, 0.35f, "jpg")).thenReturn(baos)
        `when`(imageService.createThumbnail(bufferedImage, 112)).thenReturn(bufferedImage)
        `when`(imageService.createThumbnail(bufferedImage, 224)).thenReturn(bufferedImage)
        `when`(imageService.createThumbnail(bufferedImage, 368)).thenReturn(bufferedImage)
        `when`(fileProperties.maxWidth).thenReturn(500)
        `when`(fileProperties.maxHeight).thenReturn(500)
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.IMAGE, "test-compressed.jpg",
            "jpg")).thenReturn(entity)
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.THUMBNAIL, "test-thumbnail-112.jpg",
            "jpg")).thenReturn(entity)
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.THUMBNAIL, "test-thumbnail-224.jpg",
            "jpg")).thenReturn(entity)
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.THUMBNAIL, "test-thumbnail-368.jpg",
            "jpg")).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(payload)

        verify(imageService, times(1)).createThumbnail(bufferedImage, 112)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 224)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 368)
        verify(fileRepository, times(4)).save(any())
    }

    @Test
    fun `test createThumbnailsAndUploadImage throws FileDimensionException when dimensions exceed width`() {
        val payload = FileDto(
            file = mock(),
            fileName = "image.jpg",
            fileType = FileType.IMAGE,
            userUuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val bufferedImage = mock<BufferedImage>()
        `when`(bufferedImage.width).thenReturn(600)
        `when`(bufferedImage.height).thenReturn(600)
        `when`(fileProperties.maxWidth).thenReturn(500)
        `when`(imageService.createBufferedImage(any())).thenReturn(bufferedImage)

        assertThrows<FileDimensionException> {
            fileService.storeFile(payload)
        }
    }

    @Test
    fun `test createThumbnailsAndUploadImage throws FileDimensionException when dimensions exceed height`() {
        val payload = FileDto(
            file = mock(),
            fileName = "image.jpg",
            fileType = FileType.IMAGE,
            userUuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val bufferedImage = mock<BufferedImage>()
        `when`(bufferedImage.width).thenReturn(600)
        `when`(bufferedImage.height).thenReturn(600)
        `when`(fileProperties.maxWidth).thenReturn(600)
        `when`(fileProperties.maxHeight).thenReturn(500)
        `when`(imageService.createBufferedImage(any())).thenReturn(bufferedImage)

        assertThrows<FileDimensionException> {
            fileService.storeFile(payload)
        }
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
            fileExtension = "txt"
        )

        val sdkHttpResponse = mock<SdkHttpResponse>()
        `when`(sdkHttpResponse.isSuccessful).thenReturn(true)

        val deleteResponse = mock<DeleteObjectResponse>()
        `when`(deleteResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse)

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file)
        `when`(storageService.removeFile(file)).thenReturn(deleteResponse)


        val result = fileService.removeFile(userUuid, fileUuid)

        assertTrue(result)
        verify(fileRepository).findFileByUuidAndUserUuid(fileUuid, userUuid)
        verify(storageService).removeFile(file)
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
            fileExtension = "txt"
        )

        val sdkHttpResponse = mock<SdkHttpResponse>()
        `when`(sdkHttpResponse.isSuccessful).thenReturn(false)

        val deleteResponse = mock<DeleteObjectResponse>()
        `when`(deleteResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse)

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file)
        `when`(storageService.removeFile(file)).thenReturn(deleteResponse)

        val result = fileService.removeFile(userUuid, fileUuid)

        assertFalse(result)
        verify(fileRepository).findFileByUuidAndUserUuid(fileUuid, userUuid)
        verify(storageService).removeFile(file)
    }
}