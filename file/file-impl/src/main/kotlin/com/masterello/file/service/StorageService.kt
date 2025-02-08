package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.entity.File
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ObjectIdentifier

@Service
class StorageService(private val s3Client: S3Client,
                     private val fileProperties: FileProperties
) {

    fun removeFolder(entity: File): Boolean {
        val objectKey = "${fileProperties.bucketName}/${entity.userUuid}/${entity.uuid}"

        val listRequest = ListObjectsV2Request.builder()
            .bucket(fileProperties.bucketName)
            .prefix(objectKey)
            .build()

        val filesToDelete = s3Client.listObjectsV2(listRequest)
            .contents()
            .map { ObjectIdentifier.builder().key(it.key()).build() }

        if (filesToDelete.isNotEmpty()) {
            val deleteRequest = DeleteObjectsRequest.builder()
                .bucket(fileProperties.bucketName)
                .delete { it.objects(filesToDelete) }
                .build()

            s3Client.deleteObjects(deleteRequest)
        }
        return true
    }

}