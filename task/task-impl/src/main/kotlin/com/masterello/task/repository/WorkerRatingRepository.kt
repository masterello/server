package com.masterello.task.repository

import com.masterello.task.entity.WorkerRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WorkerRatingRepository: JpaRepository<WorkerRating, UUID> {
    fun findByTaskUuid(taskUuid: UUID): WorkerRating?

    @Query(nativeQuery = true, value =
    "SELECT ROUND(AVG(wr.rating), 2) FROM worker_rating wr WHERE wr.worker_uuid = :workerUuid")
    fun calculateWorkerRating(workerUuid: UUID): Double?
}