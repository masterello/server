package com.masterello.task.mapper

import com.masterello.task.dto.ReviewDto
import com.masterello.task.dto.ReviewerType
import com.masterello.task.entity.TaskReview
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.UUID

class TaskReviewMapperTest {

    private val taskReviewMapper = TaskReviewMapper()

    @Test
    fun `mapDtoToEntity should throw IllegalArgumentException for null input`() {
        val exception = assertThrows<IllegalArgumentException> {
            taskReviewMapper.mapDtoToEntity(null)
        }
        assertEquals("WorkerReviewDto cannot be null", exception.message)
    }

    @Test
    fun `mapDtoToEntity should map ReviewDto to TaskReview correctly`() {
        val dto = ReviewDto(
            uuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            reviewerUuid = UUID.randomUUID(),
            reviewerType = ReviewerType.USER,
            taskUuid = UUID.randomUUID(),
            review = "Great work",
            rating = 5
        )

        val result = taskReviewMapper.mapDtoToEntity(dto)

        assertEquals(dto.review, result.review)
        assertEquals(dto.reviewerUuid, result.reviewerUuid)
        assertEquals(dto.taskUuid, result.taskUuid)
        assertEquals(dto.reviewerType, result.reviewerType)
    }

    @Test
    fun `mapEntityToDto should throw IllegalArgumentException for null input`() {
        val exception = assertThrows<IllegalArgumentException> {
            taskReviewMapper.mapEntityToDto(null, 5)
        }
        assertEquals("WorkerTaskReview cannot be null", exception.message)
    }

    @Test
    fun `mapEntityToDto should map TaskReview to ReviewDto correctly`() {
        val taskReview = TaskReview(
            uuid = UUID.randomUUID(),
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now(),
            reviewerUuid = UUID.randomUUID(),
            reviewerType = ReviewerType.USER,
            taskUuid = UUID.randomUUID(),
            review = "Needs improvement"
        )

        val rating = 3
        val result = taskReviewMapper.mapEntityToDto(taskReview, rating)

        assertEquals(taskReview.uuid, result.uuid)
        assertEquals(taskReview.createdDate, result.createdDate)
        assertEquals(taskReview.updatedDate, result.updatedDate)
        assertEquals(taskReview.reviewerUuid, result.reviewerUuid)
        assertEquals(taskReview.reviewerType, result.reviewerType)
        assertEquals(taskReview.taskUuid, result.taskUuid)
        assertEquals(taskReview.review, result.review)
        assertEquals(rating, result.rating)
    }
}
