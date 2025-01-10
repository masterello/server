package com.masterello.task.dto

import java.util.UUID

data class UpdateTaskDto(
    val categoryUuid: UUID?,
    val name: String?,
    val description: String?
)