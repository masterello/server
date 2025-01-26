package com.masterello.file.service

import com.masterello.file.configuration.FileProperties
import com.masterello.file.dto.*
import com.masterello.file.entity.File
import com.masterello.file.exception.*
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
        when (file.fileType) {
            in listOf(FileType.AVATAR, FileType.PORTFOLIO) -> {
                val files = fileRepository.getAllThumbnailsByParentImageUuid(file.uuid!!)
                files.map { storageService.removeFile(it) }
                fileRepository.deleteAll(files)
                return true
            }
            FileType.THUMBNAIL -> {
                val files = fileRepository.getAllThumbnailsByParentImageUuid(file.parentImage!!)
                files.map { storageService.removeFile(it) }
                fileRepository.deleteAll(files)
                return true
            }
            else -> {
                val result = storageService.removeFile(file).sdkHttpResponse().isSuccessful
                if (result) {
                    fileRepository.delete(file)
                }
                return result
            }
        }
    }

    override fun findAllFilesByUserUuid(userUUID: UUID): List<FileDto> {
        val userFiles = fileRepository.findAllFilesByUserUuid(userUUID)

        return userFiles.map { fileMapper.mapFileToDto(it) }
    }

    override fun findImagesBulk(fileType: FileType, userUuids: List<UUID>): List<BulkImageResponseDto> {
        when (fileType) {
            FileType.AVATAR, FileType.PORTFOLIO -> return findImagesByTypeBulk(fileType, userUuids)
            else -> { log.error { "Unknown file type to process: $fileType" }
            throw FileTypeException("Invalid file type provided") }
        }
    }

    private fun findImagesByTypeBulk(fileType: FileType, userUuids: List<UUID>): List<BulkImageResponseDto> {
        val files = fileRepository.findAllIAvatarsByUserUuids(fileType.code, userUuids)

        val filesByUserAndUuid = files
            .groupBy { it.userUuid }
            .mapValues { (_, userFiles) ->
                userFiles.groupBy { file ->
                    file.parentImage ?: file.uuid
                }
            }

        return userUuids.mapNotNull { userUuid ->
            val userFilesByUuid = filesByUserAndUuid[userUuid] ?: return@mapNotNull null

            val imageDtos = userFilesByUuid.map { (_, files) ->
                val images = ImageDto()

                files.forEach { file ->
                    val imageLink = constructImageLink(file)
                    when (file.thumbnailSize) {
                        SMALL -> images.small = imageLink
                        MEDIUM -> images.medium = imageLink
                        LARGE -> images.big = imageLink
                        else -> images.original = imageLink
                    }
                }
                images
            }

            if (imageDtos.isNotEmpty()) {
                BulkImageResponseDto(userUuid, imageDtos)
            } else {
                null
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

            val fileNameDto = payload.fileName?.lowercase() ?: payload.file?.originalFilename?.lowercase()
            ?: throw IllegalArgumentException("File name cannot be null or empty")

            val fileExtension = FileUtil.getFileExtension(fileNameDto)

            val entity = fileMapper.mapFileDtoToFile(payload, payload.fileType, fileNameDto, fileExtension)
            val savedEntity = fileRepository.save(entity)
            storageService.uploadFile(savedEntity, file)
            log.info { "file ${savedEntity.uuid} uploaded successfully" }
        } catch (e: S3Exception) {
            log.error { "Failed to upload file: ${payload.file?.originalFilename}, $e" }
        } catch (e: Exception) {
            log.error { "Unexpected error for file: ${payload.file?.originalFilename}, $e" }
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
                val originalFile = createAndSaveCompressedImage(bufferedImage, payload)
                createAndSaveThumbnail(bufferedImage, SMALL, payload, originalFile.uuid!!)
                createAndSaveThumbnail(bufferedImage, MEDIUM, payload, originalFile.uuid)
                createAndSaveThumbnail(bufferedImage, LARGE, payload, originalFile.uuid)
            }

        } catch (e: IOException) {
           log.error { "failed to store file $e" }
        }
    }

    private fun createAndSaveThumbnail(bufferedImage: BufferedImage, size: Int, payload: FileDto, parentUuid: UUID) {
        val thumbnail: BufferedImage = imageService.createThumbnail(bufferedImage, size)

        val originalFilename = FileUtil.getFileName(payload)
        val fileNameWithoutExtension: String = FileUtil.getFileNameWithoutExtension(originalFilename)
        val fileNameExt = "webp"

        val newImageName = "$fileNameWithoutExtension-thumbnail-$size.$fileNameExt"

        val entity = fileMapper.mapAvatarThumbnailToFile(payload, FileType.THUMBNAIL, newImageName,
            fileNameExt, size, parentUuid, null)
        val savedEntity = fileRepository.save(entity)
        storageService.uploadFile(savedEntity, thumbnail)
        log.info { "saved thumbnail for user ${payload.userUuid}" }
    }

    private fun createAndSaveCompressedImage(bufferedImage: BufferedImage, payload: FileDto): File {
        val fileName = FileUtil.getFileName(payload)
        val fileExt = "webp"
        val compressedImage = imageService.compressedImage(bufferedImage, 0.35f, fileExt)

        val fileNameWithoutExtension: String = FileUtil.getFileNameWithoutExtension(fileName)

        val newImageName = "$fileNameWithoutExtension-compressed.$fileExt"

        val entity = fileMapper.mapFileDtoToFile(payload, payload.fileType, newImageName, fileExt)
        val savedEntity = fileRepository.save(entity)

        storageService.uploadFile(savedEntity, compressedImage)
        log.info { "saved thumbnail for user ${payload.userUuid}" }
        return savedEntity
    }

    private fun constructImageLink(userFile: File): String {
        val objectKey = "${userFile.userUuid}/${userFile.uuid}.${userFile.fileExtension}"

        return fileProperties.cdnLink + objectKey
    }
}