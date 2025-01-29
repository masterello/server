package com.masterello.task.mapper

import com.masterello.task.dto.UserReviewDto
import com.masterello.task.entity.UserRating
import com.masterello.task.entity.UserTaskReview
import org.springframework.stereotype.Service

@Service
class UserTaskReviewMapper {

    fun mapDtoToEntity(dto: UserReviewDto?): UserTaskReview {
        if (dto == null) {
            throw IllegalArgumentException("UserReviewDto cannot be null")
        }

        return UserTaskReview(
            review = dto.userReview,
            userUuid = dto.userUuid,
            taskUuid = dto.taskUuid
            )
    }

    fun mapEntityToDto(taskReview: UserTaskReview?, userRating: UserRating): UserReviewDto {
        if (taskReview == null) {
            throw IllegalArgumentException("UserTaskReview cannot be null")
        }

        return UserReviewDto(
            uuid = taskReview.uuid,
            createdDate = taskReview.createdDate,
            updatedDate = taskReview.updatedDate,
            userUuid = taskReview.userUuid,
            taskUuid = taskReview.taskUuid,
            userReview = taskReview.review,
            rating = userRating.rating
        )
    }

}