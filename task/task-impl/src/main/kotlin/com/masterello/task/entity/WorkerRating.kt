package com.masterello.task.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "worker_rating", schema = "public")
data class WorkerRating(
    override val uuid: UUID? = null,
    override var taskUuid: UUID = UUID.randomUUID(),
    override var rating: Int = 1,
    override val createdDate: OffsetDateTime? = null,
    override val updatedDate: OffsetDateTime? = null
) : BaseRating(uuid, taskUuid, rating, createdDate, updatedDate)
