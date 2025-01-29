package com.masterello.task.dto

import java.time.OffsetDateTime
import java.util.*

data class UserReviewDto(
    val uuid: UUID? = null,
    val userUuid: UUID,
    val taskUuid: UUID,
    val userReview: String,
    val rating: Int,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)