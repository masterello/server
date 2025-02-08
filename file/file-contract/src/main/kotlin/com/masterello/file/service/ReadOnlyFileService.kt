package com.masterello.file.service

import com.masterello.file.dto.BulkImageResponseDto
import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileType
import java.util.UUID

interface ReadOnlyFileService {
    fun findAllImagesByUserUuid(userUUID: UUID): List<FileDto>
    fun findAllFilesByUserUuid(userUUID: UUID): List<FileDto>
    fun findImagesBulk(fileType: FileType, userUuids: List<UUID>): List<BulkImageResponseDto>
}