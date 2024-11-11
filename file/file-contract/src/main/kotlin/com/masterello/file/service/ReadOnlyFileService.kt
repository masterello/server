package com.masterello.file.service

import com.masterello.file.dto.BulkImageResponseDto
import com.masterello.file.dto.BulkImageSearchRequest
import com.masterello.file.dto.FileDto
import java.util.UUID

interface ReadOnlyFileService {
    fun findAllThumbnailsByUserUuid(userUUID: UUID): List<FileDto>
    fun findAllImagesByUserUuid(userUUID: UUID): List<FileDto>
    fun findAllFilesByUserUuid(userUUID: UUID): List<FileDto>
    fun findAvatarsByUserUuidsBulk(bulkImageSearchRequest: BulkImageSearchRequest): List<BulkImageResponseDto>
}