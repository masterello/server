package com.masterello.task.repository

import com.masterello.task.entity.TaskReview
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskReviewRepository: JpaRepository<TaskReview, UUID> {
    fun findByTaskUuidAndReviewerUuid(taskUuid: UUID, reviewerUuid: UUID): TaskReview?
    fun findByTaskUuid(taskUuid: UUID): List<TaskReview>
}