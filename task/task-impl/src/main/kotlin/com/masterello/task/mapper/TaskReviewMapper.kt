package com.masterello.task.mapper

import com.masterello.task.dto.ReviewDto
import com.masterello.task.entity.TaskReview
import org.springframework.stereotype.Service

@Service
class TaskReviewMapper {

    fun mapDtoToEntity(dto: ReviewDto?): TaskReview {
        if (dto == null) {
            throw IllegalArgumentException("WorkerReviewDto cannot be null")
        }

        return TaskReview(
            review = dto.review,
            reviewerUuid = dto.reviewerUuid,
            taskUuid = dto.taskUuid,
            reviewerType = dto.reviewerType
            )
    }

    fun mapEntityToDto(taskReview: TaskReview?, rating: Int): ReviewDto {
        if (taskReview == null) {
            throw IllegalArgumentException("WorkerTaskReview cannot be null")
        }

        return ReviewDto(
            uuid = taskReview.uuid,
            createdDate = taskReview.createdDate,
            updatedDate = taskReview.updatedDate,
            reviewerUuid = taskReview.reviewerUuid,
            reviewerType = taskReview.reviewerType,
            taskUuid = taskReview.taskUuid,
            review = taskReview.review,
            rating = rating
        )
    }

}