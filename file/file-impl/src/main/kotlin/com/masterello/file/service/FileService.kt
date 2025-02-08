package com.masterello.file.service

import com.masterello.file.dto.*
import com.masterello.file.entity.File
import com.masterello.file.exception.*
import com.masterello.file.mapper.FileMapper
import com.masterello.file.repository.FileRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.*

@Service
class FileService(private val fileRepository: FileRepository,
                  private val fileMapper: FileMapper,
                  private val storageService: StorageService
) : ReadOnlyFileService {
    private val log = KotlinLogging.logger {}

    @Transactional
    fun removeFile(userUuid: UUID, fileUUID: UUID): Boolean {
        val file = fileRepository.findFileByUuidAndUserUuid(fileUUID, userUuid)
            ?: throw NotFoundException("File for user $userUuid with uuid $fileUUID is not found")
        val result = storageService.removeFolder(file)
        if (result) {
            fileRepository.delete(file)
        }
        return result
    }

    override fun findAllFilesByUserUuid(userUUID: UUID): List<FileDto> {
        val userFiles = fileRepository.findAllFilesByUserUuid(userUUID)

        return userFiles.map { fileMapper.mapFileToDto(it) }
    }

    override fun findImagesBulk(fileType: FileType, userUuids: List<UUID>): List<BulkImageResponseDto> {
        when (fileType) {
            FileType.AVATAR, FileType.PORTFOLIO, FileType.TASK_REVIEW -> return findImagesByTypeBulk(fileType, userUuids)
            else -> { log.error { "Unknown file type to process: $fileType" }
            throw FileTypeException("Invalid file type provided") }
        }
    }

    private fun findImagesByTypeBulk(fileType: FileType, userUuids: List<UUID>): List<BulkImageResponseDto> {
        val files = fileRepository.findAllImagesByUserUuidsAndType(fileType.code, userUuids)

        val filesByUserAndUuid = files
            .groupBy { it.userUuid }

        return userUuids.mapNotNull { userUuid ->
            val userFilesByUuid = filesByUserAndUuid[userUuid] ?: return@mapNotNull null

            val imageUUID = userFilesByUuid.map { file ->
                file.uuid!!
            }

            BulkImageResponseDto(userUuid, imageUUID)
        }
    }

    override fun findAllImagesByUserUuid(userUUID: UUID): List<FileDto> {
        val userFiles = fileRepository.findAllImagesByUserUuid(userUUID)
        return userFiles.map { fileMapper.mapFileToDto(it) }
    }

    @Transactional
    fun markAsUploaded(userUuid: UUID, filesToUpdate: List<UUID>): List<FileDto> {
        val files = fileRepository.findAllFilesByIdsAndUserUuid(userUuid, filesToUpdate)
        if (files.size != filesToUpdate.size) {
            throw NotFoundException("Some files were not found")
        }

        files.map {file -> file.fileStatus = FileStatus.UPLOADED }
        val savedFiles = fileRepository.saveAll(files)
        return savedFiles.map { fileMapper.mapFileToDto(it) }
    }

    @Transactional
    fun storeFile(payloads: List<FileDto>): List<FileDto> {
        val storedFiles = mutableListOf<File>()

        for (payload in payloads) {
            try {
                when (payload.fileType) {
                    FileType.AVATAR, FileType.PORTFOLIO, FileType.TASK_REVIEW,
                    FileType.DOCUMENT, FileType.CERTIFICATE -> storedFiles.add(saveFile(payload))
                    else -> log.error { "Unknown file type to process: ${payload.fileType}" }
                }
            } catch (e: IOException) {
                log.error { "Failed to store file: ${e.message}, $e" }
            }
        }

        return storedFiles.map { fileMapper.mapFileToDto(it) }
    }

    private fun saveFile(payload: FileDto): File {
        removePreviousAvatars(payload)
        validateTask(payload)
        val entity = fileMapper.mapFileDtoToFile(payload, payload.fileType, FileStatus.UPLOADING)
        val savedEntity = fileRepository.save(entity)

        log.info { "saved image for user ${payload.userUuid}" }
        return savedEntity
    }

    private fun removePreviousAvatars(payload: FileDto) {
        if (payload.fileType == FileType.AVATAR) {
            val avatar = fileRepository.getAvatarByUserUuid(payload.userUuid)
            if(avatar.isPresent) {
                storageService.removeFolder(avatar.get())
                fileRepository.delete(avatar.get())
                log.info { "previous avatar was removed" }
            }
        }
    }

    private fun validateTask(payload: FileDto) {
        if (payload.fileType == FileType.TASK_REVIEW  && payload.taskUuid == null) {
            throw TaskNotProvidedException("Task uuid is not provided for the image")
        }
    }
}