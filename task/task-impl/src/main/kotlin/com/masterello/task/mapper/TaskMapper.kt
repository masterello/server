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
            name = dto.name,
            description = dto.description,
            userUuid = dto.userUuid,
            workerUuid = dto.workerUuid,
            categoryCode = dto.categoryCode,
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
            categoryCode = task.categoryCode,
            status = task.status
        )
    }

}