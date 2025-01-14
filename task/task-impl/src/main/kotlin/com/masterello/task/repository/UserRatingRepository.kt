package com.masterello.task.repository

import com.masterello.task.entity.UserRating
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRatingRepository: JpaRepository<UserRating, UUID> {
    fun findByTaskUuid(taskUuid: UUID): UserRating?
}