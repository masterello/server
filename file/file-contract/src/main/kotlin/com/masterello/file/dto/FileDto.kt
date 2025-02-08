package com.masterello.file.dto

import java.time.OffsetDateTime
import java.util.UUID

data class FileDto(
    val uuid: UUID? = null,
    val userUuid: UUID,
    val fileName: String,
    val fileType: FileType,
    val fileStatus: FileStatus?,
    val isPublic: Boolean = false,
    val taskUuid: UUID?,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)
