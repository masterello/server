package com.masterello.task.repository

import com.masterello.task.entity.UserTaskReview
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserTaskReviewRepository: JpaRepository<UserTaskReview, UUID> {
    fun findByTaskUuid(taskUuid: UUID): UserTaskReview?
}