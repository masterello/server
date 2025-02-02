package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import com.masterello.file.exception.FileDimensionException
import com.masterello.file.exception.FileNotProvidedException
import com.masterello.file.exception.FileTypeException
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
import org.springframework.web.multipart.MultipartFile
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
            file = null,
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
            isPublic = false,
            fileExtension = "txt"
        )

        `when`(fileRepository.findAllImagesByUserUuidsAndType(FileType.AVATAR.code, listOf(userUuid, userUuid2))).thenReturn(listOf(file))
        `when`(fileProperties.cdnLink).thenReturn("masterello.com/")
        val result = fileService.findImagesBulk(FileType.AVATAR, listOf(userUuid, userUuid2))

        assertEquals(1, result.size)
        assertEquals(userUuid, result[0].userUUID)
        assertEquals("", result[0].imageDtos[0].big)
        assertEquals("", result[0].imageDtos[0].medium)
        assertEquals("", result[0].imageDtos[0].small)
        assertEquals("masterello.com/$userUuid/$fileUuid.txt", result[0].imageDtos[0].original)
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
            fileType = FileType.AVATAR,
            isPublic = false,
            fileExtension = "txt"
        )

        `when`(fileRepository.findAllImagesByUserUuidsAndType(FileType.PORTFOLIO.code, listOf(userUuid, userUuid2))).thenReturn(listOf(file))
        `when`(fileProperties.cdnLink).thenReturn("masterello.com/")
        val result = fileService.findImagesBulk(FileType.PORTFOLIO, listOf(userUuid, userUuid2))

        assertEquals(1, result.size)
        assertEquals(userUuid, result[0].userUUID)
        assertEquals("", result[0].imageDtos[0].big)
        assertEquals("", result[0].imageDtos[0].medium)
        assertEquals("", result[0].imageDtos[0].small)
        assertEquals("masterello.com/$userUuid/$fileUuid.txt", result[0].imageDtos[0].original)
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
            taskUuid = null,
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
            taskUuid = null,
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
            fileType = FileType.AVATAR,
            fileName = "testFile",
            isPublic = true,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            taskUuid = null,
            file = null
        )

        assertThrows<FileNotProvidedException> {
            fileService.storeFile(dto)
        }
    }

    @Test
    fun `test storeFile uploads document file correctly`() {
        val multipartFile: MultipartFile = mock()
        val payload = FileDto(
            file = listOf(multipartFile),
            fileName = "test.txt",
            fileType = FileType.DOCUMENT,
            userUuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            taskUuid = UUID.randomUUID()
        )

        val entity = File(
            uuid = UUID.randomUUID(),
            userUuid = payload.userUuid,
            fileName = payload.fileName!!,
            fileType = payload.fileType,
            isPublic = false,
            taskUuid = null,
            fileExtension = FileUtil.getFileExtension(payload.fileName!!)
        )

        `when`(fileMapper.mapFileDtoToFile(payload, payload.fileType, payload.fileName!!,
            "txt")).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(payload)

        verify(storageService).uploadFile(entity, payload.file?.get(0))
    }

    @Test
    fun `test storeFile uploads image file correctly`() {
        val multipartFile: MultipartFile = mock()
        val payload = FileDto(
            file = listOf(multipartFile),
            fileName = "test.jpg",
            fileType = FileType.AVATAR,
            userUuid = UUID.randomUUID(),
            taskUuid = null,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val fileUuid = UUID.randomUUID()

        val entity = File(
            uuid = fileUuid,
            userUuid = payload.userUuid,
            fileName = payload.fileName!!,
            fileType = payload.fileType,
            isPublic = false,
            fileExtension = FileUtil.getFileExtension(payload.fileName!!)
        )
        val baos = ByteArrayOutputStream()
        val bufferedImage = BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB)
        `when`(imageService.createBufferedImage(any())).thenReturn(bufferedImage)
        `when`(imageService.compressedImage(bufferedImage, 0.35f, "webp")).thenReturn(baos)
        `when`(imageService.createThumbnail(bufferedImage, 112)).thenReturn(bufferedImage)
        `when`(imageService.createThumbnail(bufferedImage, 224)).thenReturn(bufferedImage)
        `when`(imageService.createThumbnail(bufferedImage, 368)).thenReturn(bufferedImage)
        `when`(fileProperties.maxWidth).thenReturn(500)
        `when`(fileProperties.maxHeight).thenReturn(500)
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.AVATAR, "test-compressed.webp",
            "webp")).thenReturn(entity)
        `when`(fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, "test-thumbnail-112.webp",
            "webp", 112, fileUuid)).thenReturn(entity)
        `when`(fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, "test-thumbnail-224.webp",
            "webp", 224, fileUuid)).thenReturn(entity)
        `when`(fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, "test-thumbnail-368.webp",
            "webp", 368, fileUuid)).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(payload)

        verify(imageService, times(1)).createThumbnail(bufferedImage, 112)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 224)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 368)
        verify(fileRepository, times(4)).save(any())
    }


    @Test
    fun `test storeFile uploads image file correctly and removing old avatars`() {
        val userUuid = UUID.randomUUID()
        val multipartFile: MultipartFile = mock()
        val payload = FileDto(
            file = listOf(multipartFile),
            fileName = "test.jpg",
            fileType = FileType.AVATAR,
            userUuid = userUuid,
            taskUuid = null,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val fileUuid = UUID.randomUUID()

        val entity = File(
            uuid = fileUuid,
            userUuid = payload.userUuid,
            fileName = payload.fileName!!,
            fileType = payload.fileType,
            isPublic = false,
            fileExtension = FileUtil.getFileExtension(payload.fileName!!)
        )
        val baos = ByteArrayOutputStream()
        val bufferedImage = BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB)
        `when`(fileRepository.getAllIAvatarsByUserUuid(userUuid)).thenReturn(listOf(entity))
        `when`(imageService.createBufferedImage(any())).thenReturn(bufferedImage)
        `when`(imageService.compressedImage(bufferedImage, 0.35f, "webp")).thenReturn(baos)
        `when`(imageService.createThumbnail(bufferedImage, 112)).thenReturn(bufferedImage)
        `when`(imageService.createThumbnail(bufferedImage, 224)).thenReturn(bufferedImage)
        `when`(imageService.createThumbnail(bufferedImage, 368)).thenReturn(bufferedImage)
        `when`(fileProperties.maxWidth).thenReturn(500)
        `when`(fileProperties.maxHeight).thenReturn(500)
        `when`(fileMapper.mapFileDtoToFile(payload, FileType.AVATAR, "test-compressed.webp",
            "webp")).thenReturn(entity)
        `when`(fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, "test-thumbnail-112.webp",
            "webp", 112, fileUuid)).thenReturn(entity)
        `when`(fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, "test-thumbnail-224.webp",
            "webp", 224, fileUuid)).thenReturn(entity)
        `when`(fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, "test-thumbnail-368.webp",
            "webp", 368, fileUuid)).thenReturn(entity)
        `when`(fileRepository.save(entity)).thenReturn(entity)

        fileService.storeFile(payload)

        verify(fileRepository, times(1)).deleteAll(any())
        verify(storageService, times(1)).removeFile(entity)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 112)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 224)
        verify(imageService, times(1)).createThumbnail(bufferedImage, 368)
        verify(fileRepository, times(4)).save(any())
    }

    @Test
    fun `test createThumbnailsAndUploadImage throws FileDimensionException when dimensions exceed width`() {
        val multipartFile: MultipartFile = mock()
        val payload = FileDto(
            file = listOf(multipartFile),
            fileName = "image.jpg",
            fileType = FileType.AVATAR,
            userUuid = UUID.randomUUID(),
            taskUuid = null,
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
        val multipartFile: MultipartFile = mock()
        val payload = FileDto(
            file = listOf(multipartFile),
            fileName = "image.jpg",
            fileType = FileType.AVATAR,
            userUuid = UUID.randomUUID(),
            taskUuid = null,
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
    fun `test removeUserFiles returns file deleted response`() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val fileUuid2 = UUID.randomUUID()
        val expectedFileName = "test.txt"
        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.AVATAR,
            isPublic = false,
            fileExtension = "txt"
        )

        val file2 = File(
            uuid = fileUuid2,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.THUMBNAIL,
            parentImage = fileUuid,
            isPublic = false,
            fileExtension = "txt"
        )

        val deleteResponse = mock<DeleteObjectResponse>()

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file)
        `when`(fileRepository.getAllThumbnailsByParentImageUuid(fileUuid)).thenReturn(listOf(file, file2))
        `when`(storageService.removeFile(file)).thenReturn(deleteResponse)
        `when`(storageService.removeFile(file2)).thenReturn(deleteResponse)


        val result = fileService.removeFile(userUuid, fileUuid)

        assertTrue(result)
        verify(fileRepository).getAllThumbnailsByParentImageUuid(fileUuid)
        verify(storageService).removeFile(file)
        verify(storageService).removeFile(file2)
        verify(fileRepository).deleteAll(listOf(file, file2))
    }

    @Test
    fun `test removeUserFiles by thumbnail returns file deleted response`() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val fileUuid2 = UUID.randomUUID()
        val expectedFileName = "test.txt"
        val file = File(
            uuid = fileUuid,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.AVATAR,
            isPublic = false,
            fileExtension = "txt"
        )

        val file2 = File(
            uuid = fileUuid2,
            userUuid = userUuid,
            fileName = expectedFileName,
            fileType = FileType.THUMBNAIL,
            parentImage = fileUuid,
            isPublic = false,
            fileExtension = "txt"
        )

        val deleteResponse = mock<DeleteObjectResponse>()

        `when`(fileRepository.findFileByUuidAndUserUuid(fileUuid, userUuid)).thenReturn(file2)
        `when`(fileRepository.getAllThumbnailsByParentImageUuid(fileUuid)).thenReturn(listOf(file, file2))
        `when`(storageService.removeFile(file)).thenReturn(deleteResponse)
        `when`(storageService.removeFile(file2)).thenReturn(deleteResponse)


        val result = fileService.removeFile(userUuid, fileUuid)

        assertTrue(result)
        verify(fileRepository).getAllThumbnailsByParentImageUuid(fileUuid)
        verify(storageService).removeFile(file)
        verify(storageService).removeFile(file2)
        verify(fileRepository).deleteAll(listOf(file, file2))
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