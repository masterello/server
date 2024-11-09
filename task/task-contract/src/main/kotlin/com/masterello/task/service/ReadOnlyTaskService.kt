package com.masterello.task.service

import com.masterello.task.dto.CreatedTaskDto
import com.masterello.task.dto.PageOfTaskDto
import com.masterello.task.dto.TaskDto
import com.masterello.task.dto.TaskDtoRequest
import java.util.UUID

interface ReadOnlyTaskService {
    fun createTask(createdTaskDto: CreatedTaskDto): TaskDto
    fun cancelTask(taskUuid: UUID): TaskDto
    fun completeTask(taskUuid: UUID): TaskDto
    fun updateTask(taskUuid: UUID, taskDto: TaskDto): TaskDto
    fun getTask(taskUuid: UUID): TaskDto?
    fun getUserTasks(userUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto
    fun getWorkerTasks(worker: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto
    fun getTasks(taskDtoRequest: TaskDtoRequest): PageOfTaskDto
}