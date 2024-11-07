package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.entity.File
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

@Service
class StorageService(private val s3Client: S3Client,
                     private val fileProperties: FileProperties
) {

    fun uploadFile(entity: File, file: MultipartFile?) {
        val tempFile = createTempFile(file?.inputStream)
        uploadFileToS3(entity, tempFile)

    }

    fun uploadFile(entity: File, bufferedImage: BufferedImage) {
        val tempFile = createTempFile(bufferedImage, entity.fileExtension)
        uploadFileToS3(entity, tempFile)
    }

    fun uploadFile(entity: File, byteArrayOutputStream: ByteArrayOutputStream) {
        val tempFile = createTempFile(byteArrayOutputStream)
        uploadFileToS3(entity, tempFile)
    }

    private fun uploadFileToS3(entity: File, tempFile: Path) {
        val objectKey = "${entity.userUuid}/${entity.uuid}.${entity.fileExtension}"

        val acl = if (entity.isPublic) ObjectCannedACL.PUBLIC_READ else ObjectCannedACL.PRIVATE
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(fileProperties.bucketName)
            .key(objectKey)
            .acl(acl)
            .build()

        s3Client.putObject(putObjectRequest, tempFile)
        Files.deleteIfExists(tempFile)
    }

    fun downloadFile(entity: File): ByteArray? {
        val objectKey = "${entity.userUuid}/${entity.uuid}.${entity.fileExtension}"

        val getObjectRequest = GetObjectRequest.builder()
            .bucket(fileProperties.bucketName)
            .key(objectKey)
            .build()

        val response = s3Client.getObject(getObjectRequest)
        return response.readAllBytes()
    }

    fun removeFile(entity: File): DeleteObjectResponse {
        val objectKey = "${entity.userUuid}/${entity.uuid}.${entity.fileExtension}"

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(fileProperties.bucketName)
            .key(objectKey)
            .build()

       return s3Client.deleteObject(deleteObjectRequest)
    }

    private fun createTempFile(inputStream: InputStream?): Path {
        val tempFile = Files.createTempFile(null, null)
        inputStream?.use { Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING) }
        return tempFile
    }

    private fun createTempFile(bufferedImage: BufferedImage, fileExtension: String): Path {
        val tempFile = Files.createTempFile(null, null)
        ImageIO.write(bufferedImage, fileExtension, tempFile.toFile())
        return tempFile
    }

    private fun createTempFile(byteArrayOutputStream: ByteArrayOutputStream): Path {
        val tempFile = Files.createTempFile(null, null)
        Files.write(tempFile, byteArrayOutputStream.toByteArray())
        return tempFile
    }
}