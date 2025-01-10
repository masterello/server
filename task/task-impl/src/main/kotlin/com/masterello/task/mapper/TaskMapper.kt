package com.masterello.task.mapper

import com.masterello.task.dto.TaskDto
import com.masterello.task.entity.Task
import org.springframework.stereotype.Service

@Service
class TaskMapper {

    fun mapDtoToEntity(dto: TaskDto?): Task {
        if (dto == null) {
            throw IllegalArgumentException("TaskDto cannot be null")
        }

        return Task(
            name = dto.name.lowercase(),
            description = dto.description,
            userUuid = dto.userUuid,
            workerUuid = dto.workerUuid,
            categoryUuid = dto.categoryUuid,
            status = dto.status
            )
    }

    fun mapEntityToDto(task: Task?): TaskDto {
        if (task == null) {
            throw IllegalArgumentException("Task cannot be null")
        }

        return TaskDto(
            uuid = task.uuid,
            name = task.name,
            description = task.description,
            createdDate = task.createdDate,
            updatedDate = task.updatedDate,
            userUuid = task.userUuid,
            workerUuid = task.workerUuid,
            categoryUuid = task.categoryUuid,
            status = task.status
        )
    }

}