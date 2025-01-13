package com.masterello.task.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.*

data class TaskDto(
    val uuid: UUID? = null,
    val userUuid: UUID,
    val workerUuid: UUID? = null,
    val categoryCode: Int,
    @field:NotNull
    @field:NotBlank val name: String,
    @field:NotNull
    @field:NotBlank val description: String,
    val status: TaskStatus = TaskStatus.NEW,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)