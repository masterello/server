package com.masterello.task.util

import com.masterello.auth.config.AuthConstants
import com.masterello.task.dto.*
import com.masterello.task.entity.Task
import com.masterello.task.entity.TaskReview
import com.masterello.task.entity.UserRating
import com.masterello.task.entity.WorkerRating
import io.restassured.http.Cookie
import java.time.OffsetDateTime
import java.util.*

object TestDataUtils {
    const val ACCESS_TOKEN: String =
        "eodTcZFDW4x2P95ZgiXfGK19dbz6FNgHMpOUNLF0Q9ca2GRyi7Nt-5le_tsbHFP7EA6zcCsKIxgERswmo_cWwTnz3c6WxtTSWG3PZ0SX-K7JJ00HiMx4SBu2ESo4LcZH"

    const val ADMIN: String = "e5fcf8dd-b6be-4a36-a85a-e2d952cc9999"
    const val WORKER1: String = "e5fcf8dd-b6be-4a36-a85a-e2d952cc6254"
    const val WORKER2: String = "e4de38bf-168e-41fc-b7b1-b9d74a47529e"
    const val USER1: String = "e5fcf8dd-b6be-4a36-a85a-e2d952cc6200"
    const val USER2: String = "0c018736-49e5-4611-8722-d2ecd0567fb1"
    const val USER_NO_TASKS: String = "769c2113-8b6a-4dfb-8549-fd3de527e226"
    val TASK_UUID: UUID = UUID.fromString("a45fb214-7c41-4a3d-a990-b499577d46c4")
    val TASK_UUID2: UUID = UUID.fromString("a99fb214-7c41-4a3d-a990-b499577d46c5")
    fun createTask(
        uuid: UUID = UUID.randomUUID(),
        userUuid: UUID = UUID.randomUUID(),
        workerUuid: UUID? = null,
        name: String = "Sample Task",
        description: String = "A test description",
        categoryCode: Int = 1,
        status: TaskStatus = TaskStatus.NEW,
        createdDate: OffsetDateTime = OffsetDateTime.now(),
        updatedDate: OffsetDateTime = OffsetDateTime.now()
    ) = Task(
        uuid = uuid,
        name = name,
        description = description,
        userUuid = userUuid,
        workerUuid = workerUuid,
        categoryCode = categoryCode,
        status = status,
        createdDate = createdDate,
        updatedDate = updatedDate
    )

    fun createTaskDto(
        uuid: UUID = UUID.randomUUID(),
        userUuid: UUID = UUID.randomUUID(),
        workerUuid: UUID? = null,
        name: String = "sample task",
        description: String = "A test description",
        categoryCode: Int = 1,
        status: TaskStatus = TaskStatus.NEW,
        createdDate: OffsetDateTime = OffsetDateTime.now(),
        updatedDate: OffsetDateTime = OffsetDateTime.now()
    ) = TaskDto(
        uuid = uuid,
        name = name,
        description = description,
        createdDate = createdDate,
        updatedDate = updatedDate,
        userUuid = userUuid,
        workerUuid = workerUuid,
        categoryCode = categoryCode,
        status = status
    )

    fun updateTaskDto(name: String = "sample task",
                      description: String = "A test description",
                      categoryCode: Int = 1
    ) = UpdateTaskDto(
        name = name,
        description = description,
        categoryCode = categoryCode
    )

    fun getReviewDto(taskUuid: UUID = UUID.randomUUID(),
                  uuid: UUID = UUID.randomUUID(),
                  reviewerUuid: UUID = UUID.randomUUID(),
                  reviewerType: ReviewerType = ReviewerType.WORKER,
                  review: String = "Good job!",
                  rating: Int = 5,
                  createdDate: OffsetDateTime = OffsetDateTime.now(),
                  updatedDate: OffsetDateTime = OffsetDateTime.now()) =
        ReviewDto(
            uuid = uuid,
            taskUuid = taskUuid,
            reviewerUuid = reviewerUuid,
            reviewerType = reviewerType,
            review = review,
            rating = rating,
            createdDate = createdDate,
            updatedDate = updatedDate
    )

    fun getReview(taskUuid: UUID = UUID.randomUUID(),
               uuid: UUID = UUID.randomUUID(),
               reviewerUuid: UUID = UUID.randomUUID(),
               reviewerType: ReviewerType = ReviewerType.WORKER,
               review: String = "Good job!",
               createdDate: OffsetDateTime = OffsetDateTime.now(),
               updatedDate: OffsetDateTime = OffsetDateTime.now()) =
        TaskReview(
            uuid = uuid,
            taskUuid = taskUuid,
            reviewerUuid = reviewerUuid,
            reviewerType = reviewerType,
            review = review,
            createdDate = createdDate,
            updatedDate = updatedDate
        )

    fun getUserReview(taskUuid: UUID = UUID.randomUUID(),
                  uuid: UUID = UUID.randomUUID(),
                  workerUuid: UUID = UUID.randomUUID(),
                  userUuid: UUID = UUID.randomUUID(),
                  rating: Int = 5,
                  createdDate: OffsetDateTime = OffsetDateTime.now(),
                  updatedDate: OffsetDateTime = OffsetDateTime.now()) =
        UserRating(
            uuid = uuid,
            taskUuid = taskUuid,
            workerUuid = workerUuid,
            userUuid = userUuid,
            rating = rating,
            createdDate = createdDate,
            updatedDate = updatedDate
        )

    fun getWorkerReview(taskUuid: UUID = UUID.randomUUID(),
                        uuid: UUID = UUID.randomUUID(),
                        workerUuid: UUID = UUID.randomUUID(),
                        userUuid: UUID = UUID.randomUUID(),
                        rating: Int = 5,
                        createdDate: OffsetDateTime = OffsetDateTime.now(),
                        updatedDate: OffsetDateTime = OffsetDateTime.now()) =
        WorkerRating(
            uuid = uuid,
            taskUuid = taskUuid,
            workerUuid = workerUuid,
            userUuid = userUuid,
            rating = rating,
            createdDate = createdDate,
            updatedDate = updatedDate
        )

    fun tokenCookie(): Cookie {
        return Cookie.Builder(AuthConstants.M_TOKEN_COOKIE, ACCESS_TOKEN).build()
    }
}
