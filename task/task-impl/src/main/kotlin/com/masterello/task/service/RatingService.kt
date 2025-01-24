package com.masterello.task.service

import com.masterello.task.dto.ReviewDto
import com.masterello.task.entity.UserRating
import com.masterello.task.entity.WorkerRating
import com.masterello.task.repository.UserRatingRepository
import com.masterello.task.repository.WorkerRatingRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class RatingService(private val workerRatingRepository: WorkerRatingRepository,
                    private val userRatingRepository: UserRatingRepository) {
    private val log = KotlinLogging.logger {}

    fun calculateWorkerRating(workerUuid: UUID): String {
        val rating = workerRatingRepository.calculateWorkerRating(workerUuid)
        if (rating == null || rating == 0.0) {
            return "No ratings yet"
        }
        return rating.toString()
    }

    fun calculateUserRating(userUuid: UUID): String {
        val rating = userRatingRepository.calculateUserRating(userUuid)
        if (rating == null || rating == 0.0) {
            return "No ratings yet"
        }
        return rating.toString()
    }


    fun makeWorkerRating(taskUuid: UUID, workerUuid: UUID, reviewDto: ReviewDto) : WorkerRating {
        var taskRating = workerRatingRepository.findByTaskUuid(taskUuid)

        if (taskRating == null) {
            taskRating = WorkerRating(rating = reviewDto.rating, taskUuid = reviewDto.taskUuid,
                workerUuid = workerUuid, userUuid = reviewDto.reviewerUuid)
        } else {
            log.info { "Updating worker rating by user for task $taskUuid" }
            taskRating.rating = reviewDto.rating
        }

        return workerRatingRepository.saveAndFlush(taskRating)
    }

    fun makeUserRating(taskUuid: UUID, userUuid: UUID, reviewDto: ReviewDto) : UserRating {
        var taskRating = userRatingRepository.findByTaskUuid(taskUuid)

        if (taskRating == null) {
            taskRating = UserRating(rating = reviewDto.rating, taskUuid = reviewDto.taskUuid,
                workerUuid = reviewDto.reviewerUuid, userUuid = userUuid)
        } else {
            log.info { "Updating user rating by worker for task $taskUuid" }
            taskRating.rating = reviewDto.rating
        }

        return userRatingRepository.saveAndFlush(taskRating)
    }
}