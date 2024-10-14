package com.masterello.file.service

import com.masterello.file.dto.FileDto
import java.util.UUID

interface ReadOnlyFileService {

    fun downloadUserFile(userUuid: UUID, fileUUID: UUID): Pair<String, ByteArray?>?
    fun removeFile(userUuid: UUID, fileUUID: UUID): Boolean
    fun findAllFilesByUserUuid(userUUID: UUID): List<FileDto>
    fun storeFile(payload: FileDto)
}