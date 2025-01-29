package com.masterello.task.repository

import com.masterello.task.entity.WorkerTaskReview
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkerTaskReviewRepository: JpaRepository<WorkerTaskReview, UUID> {
    fun findByTaskUuid(taskUuid: UUID): WorkerTaskReview?
}