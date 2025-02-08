package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.entity.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object
import java.util.*


@ExtendWith(MockitoExtension::class)
internal class StorageServiceTest {
    @Mock
    private val s3Client: S3Client? = null

    @Mock
    private val fileProperties: FileProperties? = null

    @InjectMocks
    private val storageService: StorageService? = null

    @Captor
    private val listRequestCaptor: ArgumentCaptor<ListObjectsV2Request>? = null

    @Captor
    private val deleteRequestCaptor: ArgumentCaptor<DeleteObjectsRequest>? = null

    @Test
    fun removeFolder_shouldDeleteAllFilesInFolder() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val bucketName = "test-bucket"

        val entity: File = mock(File::class.java)
        `when`(entity.userUuid).thenReturn(userUuid)
        `when`(entity.uuid).thenReturn(fileUuid)

        `when`(fileProperties!!.bucketName).thenReturn(bucketName)

        val s3Object = S3Object.builder().key("$userUuid/$fileUuid/file1.txt").build()
        val listResponse = ListObjectsV2Response.builder().contents(s3Object).build()

        `when`(
            s3Client!!.listObjectsV2(
                ArgumentMatchers.any(
                    ListObjectsV2Request::class.java
                )
            )
        ).thenReturn(listResponse)

        val result = storageService!!.removeFolder(entity)

        verify(s3Client).listObjectsV2(listRequestCaptor!!.capture())
       assertEquals(bucketName, listRequestCaptor.value.bucket())
       assertEquals("test-bucket/$userUuid/$fileUuid", listRequestCaptor.value.prefix())

        verify(s3Client).deleteObjects(deleteRequestCaptor!!.capture())
        assertEquals(bucketName, deleteRequestCaptor.value.bucket())
        assertEquals(1, deleteRequestCaptor.value.delete().objects().size)
        assertEquals("$userUuid/$fileUuid/file1.txt", deleteRequestCaptor.value.delete().objects()[0].key())

        assertTrue(result)
    }

    @Test
    fun removeFolder_shouldReturnTrueWhenFolderIsEmpty() {
        val userUuid = UUID.randomUUID()
        val fileUuid = UUID.randomUUID()
        val bucketName = "test-bucket"

        val entity: File = mock(File::class.java)
        `when`(entity.userUuid).thenReturn(userUuid)
        `when`(entity.uuid).thenReturn(fileUuid)

        `when`(fileProperties!!.bucketName).thenReturn(bucketName)

        val listResponse = ListObjectsV2Response.builder().contents(emptyList()).build()

        `when`(
            s3Client!!.listObjectsV2(
                ArgumentMatchers.any(
                    ListObjectsV2Request::class.java
                )
            )
        ).thenReturn(listResponse)

        val result = storageService!!.removeFolder(entity)

        verify(s3Client).listObjectsV2(
            ArgumentMatchers.any(
                ListObjectsV2Request::class.java
            )
        )
        verify(s3Client, never()).deleteObjects(
            ArgumentMatchers.any(
                DeleteObjectsRequest::class.java
            )
        )

        assertTrue(result)
    }
}