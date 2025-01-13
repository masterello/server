package com.masterello.task.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.*

data class ReviewDto(
    val uuid: UUID? = null,
    val reviewerUuid: UUID,
    val reviewerType: ReviewerType,
    val taskUuid: UUID,
    @field:NotNull
    @field:NotBlank val review: String,
    @field:Min(1)
    @field:Max(5) val rating: Int,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)