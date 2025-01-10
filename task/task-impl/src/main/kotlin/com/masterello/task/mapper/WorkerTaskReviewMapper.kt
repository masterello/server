package com.masterello.task.mapper

import com.masterello.task.dto.WorkerReviewDto
import com.masterello.task.entity.WorkerRating
import com.masterello.task.entity.WorkerTaskReview
import org.springframework.stereotype.Service

@Service
class WorkerTaskReviewMapper {

    fun mapDtoToEntity(dto: WorkerReviewDto?): WorkerTaskReview {
        if (dto == null) {
            throw IllegalArgumentException("WorkerReviewDto cannot be null")
        }

        return WorkerTaskReview(
            review = dto.workerReview,
            workerUuid = dto.workerUuid,
            taskUuid = dto.taskUuid
            )
    }

    fun mapEntityToDto(taskReview: WorkerTaskReview?, workerRating: WorkerRating): WorkerReviewDto {
        if (taskReview == null) {
            throw IllegalArgumentException("WorkerTaskReview cannot be null")
        }

        return WorkerReviewDto(
            uuid = taskReview.uuid,
            createdDate = taskReview.createdDate,
            updatedDate = taskReview.updatedDate,
            workerUuid = taskReview.workerUuid,
            taskUuid = taskReview.taskUuid,
            workerReview = taskReview.review,
            rating = workerRating.rating
        )
    }

}