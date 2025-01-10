package com.masterello.task.repository

import com.masterello.task.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskRepository: JpaRepository<Task, UUID> {
    fun findAllByUserUuid(userUuid: UUID, pageable: Pageable): Page<Task>
    fun findAllByWorkerUuid(workerUuid: UUID, pageable: Pageable): Page<Task>


    @Query(nativeQuery = true, value =
        "SELECT COUNT(t) FROM task t WHERE t.worker_uuid = :workerUuid AND (t.status = 3 OR t.status = 5)")
    fun countByWorkerUuidAndCompletedStatus(workerUuid: UUID): Long
}