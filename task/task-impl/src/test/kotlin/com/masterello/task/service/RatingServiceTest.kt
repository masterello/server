package com.masterello.task.service

import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.task.repository.UserRatingRepository
import com.masterello.task.repository.WorkerRatingRepository
import com.masterello.task.util.TestDataUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatingServiceTest {
    @Mock
    private lateinit var workerRatingRepository: WorkerRatingRepository

    @Mock
    private lateinit var userRatingRepository: UserRatingRepository


    @Mock
    private lateinit var authentication: MasterelloAuthentication

    @Mock
    private lateinit var securityContext: SecurityContext

    @InjectMocks
    private lateinit var ratingService: RatingService

    @BeforeEach
    fun setup() {
        SecurityContextHolder.setContext(securityContext)
        `when`(securityContext.authentication).thenReturn(authentication)
    }

    @Test
    fun `calculateUserRating should return no rating when ratings are not found`() {
        val userUuid = UUID.randomUUID()
        `when`(userRatingRepository.calculateUserRating(userUuid)).thenReturn(0.0)

        val result = ratingService.calculateUserRating(userUuid)

        assertEquals("No ratings yet", result)
        verify(userRatingRepository).calculateUserRating(userUuid)
    }

    @Test
    fun `calculateUserRating should return rating when ratings are found`() {
        val userUuid = UUID.randomUUID()
        `when`(userRatingRepository.calculateUserRating(userUuid)).thenReturn(4.7)

        val result = ratingService.calculateUserRating(userUuid)

        assertEquals("4.7", result)
        verify(userRatingRepository).calculateUserRating(userUuid)
    }

    @Test
    fun `calculateWorkerRating should return no rating when ratings are not found`() {
        val workerUuid = UUID.randomUUID()
        `when`(workerRatingRepository.calculateWorkerRating(workerUuid)).thenReturn(0.0)

        val result = ratingService.calculateWorkerRating(workerUuid)

        assertEquals("No ratings yet", result)
        verify(workerRatingRepository).calculateWorkerRating(workerUuid)
    }

    @Test
    fun `calculateWorkerRating should return rating when ratings are found`() {
        val userUuid = UUID.randomUUID()
        `when`(workerRatingRepository.calculateWorkerRating(userUuid)).thenReturn(4.5)

        val result = ratingService.calculateWorkerRating(userUuid)

        assertEquals("4.5", result)
        verify(workerRatingRepository).calculateWorkerRating(userUuid)
    }

    @Test
    fun `makeWorkerRating should create new rating when rating is not found`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val review = TestDataUtils.getWorkerReview()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = userUuid)
        `when`(workerRatingRepository.findByTaskUuid(taskUuid)).thenReturn(null)
        `when`(workerRatingRepository.saveAndFlush(any())).thenReturn(review)

        val result = ratingService.makeWorkerRating(taskUuid, workerUuid, reviewDto)

        assertEquals(5, result.rating)
        verify(workerRatingRepository).findByTaskUuid(taskUuid)
    }

    @Test
    fun `makeWorkerRating should update rating when rating is found`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val review = TestDataUtils.getWorkerReview()
        val updatedReview = TestDataUtils.getWorkerReview(rating = 3)

        val reviewDto = TestDataUtils.getReviewDto(rating = 3)
        `when`(workerRatingRepository.findByTaskUuid(taskUuid)).thenReturn(review)
        `when`(workerRatingRepository.saveAndFlush(any())).thenReturn(updatedReview)


        val result = ratingService.makeWorkerRating(taskUuid, workerUuid, reviewDto)

        assertEquals(3, result.rating)
        verify(workerRatingRepository).findByTaskUuid(taskUuid)
    }

    @Test
    fun `makeUserRating should create new rating when rating is not found`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val review = TestDataUtils.getUserReview(taskUuid = taskUuid, userUuid = userUuid)
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = workerUuid)
        `when`(userRatingRepository.findByTaskUuid(taskUuid)).thenReturn(null)
        `when`(userRatingRepository.saveAndFlush(any())).thenReturn(review)

        val result = ratingService.makeUserRating(taskUuid, userUuid, reviewDto)

        assertEquals(5, result.rating)
        assertEquals(taskUuid, result.taskUuid)
        assertEquals(userUuid, result.userUuid)
        verify(userRatingRepository).findByTaskUuid(taskUuid)
    }

    @Test
    fun `makeUserRating should update rating when rating is found`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val review = TestDataUtils.getUserReview()
        val updatedReview = TestDataUtils.getUserReview(rating = 3)
        val reviewDto = TestDataUtils.getReviewDto(rating = 3)
        `when`(userRatingRepository.findByTaskUuid(taskUuid)).thenReturn(review)
        `when`(userRatingRepository.saveAndFlush(any())).thenReturn(updatedReview)

        val result = ratingService.makeUserRating(taskUuid, workerUuid, reviewDto)

        assertEquals(3, result.rating)
        verify(userRatingRepository).findByTaskUuid(taskUuid)
    }
}