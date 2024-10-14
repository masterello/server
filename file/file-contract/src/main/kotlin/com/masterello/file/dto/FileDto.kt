package com.masterello.file.dto

import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime
import java.util.UUID

data class FileDto(
    val uuid: UUID? = null,
    val userUuid: UUID,
    val fileName: String?,
    val fileType: FileType,
    val isPublic: Boolean = false,
    val file: MultipartFile?,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)
