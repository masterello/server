package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.dto.*
import com.masterello.file.entity.File
import com.masterello.file.exception.FileDimensionException
import com.masterello.file.exception.FileNotProvidedException
import com.masterello.file.exception.NotFoundException
import com.masterello.file.mapper.FileMapper
import com.masterello.file.repository.FileRepository
import com.masterello.file.util.FileUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.S3Exception
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.*

@Service
open class FileService(private val fileRepository: FileRepository,
                  private val fileMapper: FileMapper,
                  private val imageService: ImageService,
                  private val fileProperties: FileProperties,
                  private val storageService: StorageService
) : ReadOnlyFileService {
    private val log = KotlinLogging.logger {}

    companion object {
        const val SMALL: Int = 112
        const val MEDIUM: Int = 224
        const val LARGE: Int = 368
    }

    fun downloadUserFile(userUuid: UUID, fileUUID: UUID): Pair<String, ByteArray?>? {
        val file = fileRepository.findFileByUuidAndUserUuid(fileUUID, userUuid)

        return file?.let {
            val fileContent = storageService.downloadFile(it)
            it.fileName to fileContent
        }
    }

    @Transactional
    fun removeFile(userUuid: UUID, fileUUID: UUID): Boolean {
        val file = fileRepository.findFileByUuidAndUserUuid(fileUUID, userUuid)
            ?: throw NotFoundException("File for user $userUuid with uuid $fileUUID is not found")

        val result = storageService.removeFile(file).sdkHttpResponse().isSuccessful
        if (result) {
            fileRepository.delete(file)
        }
        return result
    }

    override fun findAllFilesByUserUuid(userUUID: UUID): List<FileDto> {
        val userFiles = fileRepository.findAllFilesByUserUuid(userUUID)

        return userFiles.map { fileMapper.mapFileToDto(it) }
    }

    override fun findAvatarsByUserUuidsBulk(bulkImageSearchRequest: BulkImageSearchRequest): List<BulkImageResponseDto> {
        when (bulkImageSearchRequest.fileType) {
            FileType.AVATAR -> return findAvatarsBulk(bulkImageSearchRequest)
            else -> { log.error { "Unknown file type to process: ${bulkImageSearchRequest.fileType}" }
            return emptyList() }
        }
    }

    private fun findAvatarsBulk(bulkImageSearchRequest: BulkImageSearchRequest): List<BulkImageResponseDto> {
        val userUuids = bulkImageSearchRequest.userUuids
        val files = fileRepository.findAllIAvatarsByUserUuids(userUuids)
        val filesByUserUuid = files.groupBy { it.userUuid }

        return userUuids.mapNotNull { userUuid ->
            val userFiles = filesByUserUuid[userUuid]
            if (userFiles.isNullOrEmpty()) {
                null
            } else {
                val avatars = AvatarDto()
                userFiles.forEach { userFile ->
                    val avatarLink = constructAvatarLink(userFile)
                    when (userFile.thumbailSize) {
                        SMALL -> avatars.small = avatarLink
                        MEDIUM -> avatars.medium = avatarLink
                        LARGE -> avatars.big = avatarLink
                        else -> avatars.original = avatarLink
                    }
                }
                BulkImageResponseDto(userUuid, avatars)
            }
        }
    }

    override fun findAllThumbnailsByUserUuid(userUUID: UUID): List<FileDto> {
        val userFiles = fileRepository.findAllThumbnailsByUserUuid(userUUID)

        return userFiles.map { fileMapper.mapFileToDto(it) }
    }

    override fun findAllImagesByUserUuid(userUUID: UUID): List<FileDto> {
        val userFiles = fileRepository.findAllImagesByUserUuid(userUUID)

        return userFiles.map { fileMapper.mapFileToDto(it) }
    }

    @Transactional
    fun storeFile(payload: FileDto) {
        try {
            payload.file ?: throw FileNotProvidedException("File is not provided in the request")

            when (payload.fileType) {
                FileType.AVATAR, FileType.PORTFOLIO -> createThumbnailsAndUploadAvatar(payload)
                FileType.DOCUMENT, FileType.CERTIFICATE -> storeAndUploadDocument(payload)
                else -> log.error { "Unknown file type to process: ${payload.fileType}" }
            }
        } catch (e: IOException) {
            log.error { "Failed to store file: ${e.message}, $e"}
        }
    }

    private fun storeAndUploadDocument(payload: FileDto) {
        try {
            val file = payload.file ?: throw IllegalArgumentException("File cannot be null")

            val fileNameDto = payload.fileName?.lowercase() ?: file.originalFilename?.lowercase()
            ?: throw IllegalArgumentException("File name cannot be null or empty")

            val fileNameExt = FileUtil.getFileExtension(fileNameDto)

            val entity = fileMapper.mapFileDtoToFile(payload, payload.fileType, fileNameDto, fileNameExt)
            val savedEntity = fileRepository.save(entity)
            storageService.uploadFile(savedEntity, file)
            log.info { "file ${savedEntity.uuid} uploaded successfully" }
        } catch (e: S3Exception) {
            log.error { "Failed to upload file, $e" }
        }
    }

    private fun createThumbnailsAndUploadAvatar(payload: FileDto) {
        if (payload.fileType == FileType.AVATAR) {
            val files = fileRepository.getAllIAvatarsByUserUuid(payload.userUuid)

            files.map { storageService.removeFile(it) }

            fileRepository.deleteAll(files)
            log.info { "all previous avatars were removed" }
        }
        return try {
            val file = payload.file
            val bufferedImage: BufferedImage = imageService.createBufferedImage(file)

            val width = bufferedImage.width
            val height = bufferedImage.height

            if (width > fileProperties.maxWidth ||
                height > fileProperties.maxHeight) {
                throw FileDimensionException("Provided image exceeds max width and height")
            } else {
                createAndSaveCompressedImage(bufferedImage, payload)
                createAndSaveThumbnail(bufferedImage, SMALL, payload)
                createAndSaveThumbnail(bufferedImage, MEDIUM, payload)
                createAndSaveThumbnail(bufferedImage, LARGE, payload)
            }

        } catch (e: IOException) {
           log.error { "failed to store file $e" }
        }
    }

    private fun createAndSaveThumbnail(bufferedImage: BufferedImage, size: Int, payload: FileDto) {
        val thumbnail: BufferedImage = imageService.createThumbnail(bufferedImage, size)

        val originalFilename = FileUtil.getFileName(payload)
        val fileNameWithoutExtension: String = FileUtil.getFileNameWithoutExtension(originalFilename)
        val fileNameExt = "webp"
        val isAvatar = payload.fileType == FileType.AVATAR

        val newImageName = "$fileNameWithoutExtension-thumbnail-$size.$fileNameExt"

        val entity = fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, newImageName,
            fileNameExt, size, isAvatar)
        val savedEntity = fileRepository.save(entity)
        storageService.uploadFile(savedEntity, thumbnail)
        log.info { "saved thumbnail for user ${payload.userUuid}" }
    }

    private fun createAndSaveCompressedImage(bufferedImage: BufferedImage, payload: FileDto) {
        val fileName = FileUtil.getFileName(payload)
        val fileExt = "webp"
        val compressedImage = imageService.compressedImage(bufferedImage, 0.35f, fileExt)

        val fileNameWithoutExtension: String = FileUtil.getFileNameWithoutExtension(fileName)

        val newImageName = "$fileNameWithoutExtension-compressed.$fileExt"

        val entity = fileMapper.mapFileDtoToFile(payload, payload.fileType, newImageName, fileExt)
        val savedEntity = fileRepository.save(entity)

        storageService.uploadFile(savedEntity, compressedImage)
        log.info { "saved thumbnail for user ${payload.userUuid}" }
    }

    private fun constructAvatarLink(userFile: File): String {
        val objectKey = "${userFile.userUuid}/${userFile.uuid}.${userFile.fileExtension}"

        return fileProperties.cdnLink + objectKey
    }
}