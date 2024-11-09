package com.masterello.task.dto

import java.time.OffsetDateTime
import java.util.*

data class TaskDto(
    val uuid: UUID? = null,
    val userUuid: UUID,
    val workerUuid: UUID? = null,
    val categoryId: UUID,
    val name: String,
    val description: String,
    val status: TaskStatus,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
    )