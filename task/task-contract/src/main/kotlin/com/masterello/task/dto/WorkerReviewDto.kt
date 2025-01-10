package com.masterello.task.dto

import java.time.OffsetDateTime
import java.util.*

data class WorkerReviewDto(
    val uuid: UUID? = null,
    val workerUuid: UUID,
    val taskUuid: UUID,
    val workerReview: String,
    val rating: Int,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)