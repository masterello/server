package com.masterello.task.service

import com.masterello.task.dto.PageOfTaskDto
import com.masterello.task.dto.TaskDto
import com.masterello.task.dto.TaskDtoRequest
import java.util.UUID

interface ReadOnlyTaskService {
    fun getTask(taskUuid: UUID): TaskDto
    fun getUserTasks(userUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto
    fun getWorkerTasks(workerUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto
    fun getTasks(taskDtoRequest: TaskDtoRequest): PageOfTaskDto
}