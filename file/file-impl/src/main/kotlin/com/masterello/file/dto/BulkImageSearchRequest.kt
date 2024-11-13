package com.masterello.file.dto

import com.masterello.file.validator.SearchableImage
import java.util.UUID

data class BulkImageSearchRequest (
    @field:SearchableImage
    val fileType: FileType,
    val userUuids: List<UUID>
)