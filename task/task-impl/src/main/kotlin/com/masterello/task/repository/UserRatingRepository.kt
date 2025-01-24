package com.masterello.task.repository

import com.masterello.task.entity.UserRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRatingRepository: JpaRepository<UserRating, UUID> {
    fun findByTaskUuid(taskUuid: UUID): UserRating?

    @Query(nativeQuery = true, value =
    "SELECT ROUND(AVG(ur.rating), 2) FROM user_rating ur WHERE ur.user_uuid = :userUuid")
    fun calculateUserRating(userUuid: UUID): Double?
}