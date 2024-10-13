package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class StorageServiceTest {

    @Mock
    private lateinit var s3Client: S3Client

    @Mock
    private lateinit var fileProperties: FileProperties

    @InjectMocks
    private lateinit var storageService: StorageService

    @Test
    fun `test uploadFile with MultipartFile`() {
        val file: MultipartFile = mock()
        val inputStream: InputStream = mock()
        `when`(file.inputStream).thenReturn(inputStream)

        val entity = File(UUID.randomUUID(), UUID.randomUUID(), "test.png", "png",
            true, FileType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now())
        `when`(fileProperties.bucketName).thenReturn("test-bucket")

        storageService.uploadFile(entity, file)

        verify(s3Client, times(1)).putObject(any<PutObjectRequest>(), any<Path>())
    }

    @Test
    fun `test uploadFile with BufferedImage`() {
        val bufferedImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)

        val entity = File(UUID.randomUUID(), UUID.randomUUID(), "test.png", "png",
            true, FileType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now())
        `when`(fileProperties.bucketName).thenReturn("test-bucket")

        storageService.uploadFile(entity, bufferedImage)

        verify(s3Client, times(1)).putObject(any<PutObjectRequest>(), any<Path>())
    }

    @Test
    fun `test uploadFile with ByteArrayOutputStream`() {
        val byteArrayOutputStream = ByteArrayOutputStream()

        val entity = File(UUID.randomUUID(), UUID.randomUUID(), "test.png", "png",
            true, FileType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now())
        `when`(fileProperties.bucketName).thenReturn("test-bucket")

        storageService.uploadFile(entity, byteArrayOutputStream)

        verify(s3Client, times(1)).putObject(any<PutObjectRequest>(), any<Path>())
    }

    @Test
    fun `test downloadFile`() {
        val entity = File(UUID.randomUUID(), UUID.randomUUID(), "test.png", "png",
            true, FileType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now())
        val mockResponseInputStream: ResponseInputStream<GetObjectResponse> = mock()

        `when`(fileProperties.bucketName).thenReturn("test-bucket")
        `when`(s3Client.getObject(any<GetObjectRequest>())).thenReturn(mockResponseInputStream)

        storageService.downloadFile(entity)

        verify(s3Client, times(1)).getObject(any<GetObjectRequest>())
    }

    @Test
    fun `test removeFile deletes the file from S3`() {
        // Given
        val file = File(
            uuid = UUID.randomUUID(),
            userUuid = UUID.randomUUID(),
            fileName = "test.jpg",
            fileType = FileType.IMAGE,
            isPublic = false,
            fileExtension = "jpg"
        )

        val bucketName = "test-bucket"
        `when`(fileProperties.bucketName).thenReturn(bucketName)
        `when`(s3Client.deleteObject(any<DeleteObjectRequest>())).thenReturn(DeleteObjectResponse.builder()
            .deleteMarker(true)
            .versionId("1")
            .build())

        // When
        val result = storageService.removeFile(file)

        // Then
        verify(s3Client).deleteObject(any<DeleteObjectRequest>())
        assertEquals(result.deleteMarker(), true)
    }
}