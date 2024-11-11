package com.masterello.file.dto

import java.util.UUID

data class BulkImageSearchRequest (
    val fileType: FileType,
    val userUuids: List<UUID>
)