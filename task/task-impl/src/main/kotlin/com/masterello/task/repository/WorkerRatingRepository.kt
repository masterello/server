package com.masterello.task.repository

import com.masterello.task.entity.WorkerRating
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkerRatingRepository: JpaRepository<WorkerRating, UUID> {
    fun findByTaskUuid(taskUuid: UUID): WorkerRating?
}