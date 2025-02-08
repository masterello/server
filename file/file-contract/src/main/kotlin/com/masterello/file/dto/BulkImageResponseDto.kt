package com.masterello.file.dto

import java.util.UUID

data class BulkImageResponseDto (
    val userUUID: UUID,
    val imageDtos: List<UUID>
)