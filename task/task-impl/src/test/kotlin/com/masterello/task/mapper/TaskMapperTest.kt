package com.masterello.task.mapper

import com.masterello.task.dto.TaskDto
import com.masterello.task.dto.TaskStatus
import com.masterello.task.entity.Task
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.UUID

class TaskMapperTest {

    private val taskMapper = TaskMapper()

    @Test
    fun `mapDtoToEntity should throw IllegalArgumentException for null input`() {
        val exception = assertThrows<IllegalArgumentException> {
            taskMapper.mapDtoToEntity(null)
        }
        assertEquals("TaskDto cannot be null", exception.message)
    }

    @Test
    fun `mapDtoToEntity should map TaskDto to Task correctly`() {
        val dto = TaskDto(
            uuid = UUID.randomUUID(),
            name = "Test Task",
            description = "A task description",
            userUuid = UUID.randomUUID(),
            workerUuid = UUID.randomUUID(),
            categoryCode = 1,
            status = TaskStatus.CANCELLED,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val result = taskMapper.mapDtoToEntity(dto)

        assertEquals(dto.name, result.name)
        assertEquals(dto.description, result.description)
        assertEquals(dto.userUuid, result.userUuid)
        assertEquals(dto.workerUuid, result.workerUuid)
        assertEquals(dto.categoryCode, result.categoryCode)
        assertEquals(dto.status, result.status)
    }

    @Test
    fun `mapEntityToDto should throw IllegalArgumentException for null input`() {
        val exception = assertThrows<IllegalArgumentException> {
            taskMapper.mapEntityToDto(null)
        }
        assertEquals("Task cannot be null", exception.message)
    }

    @Test
    fun `mapEntityToDto should map Task to TaskDto correctly`() {
        val task = Task(
            uuid = UUID.randomUUID(),
            name = "mapped task",
            description = "task description",
            userUuid = UUID.randomUUID(),
            workerUuid = UUID.randomUUID(),
            categoryCode = 2,
            status = TaskStatus.IN_REVIEW,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )

        val result = taskMapper.mapEntityToDto(task)

        assertEquals(task.uuid, result.uuid)
        assertEquals(task.name, result.name)
        assertEquals(task.description, result.description)
        assertEquals(task.userUuid, result.userUuid)
        assertEquals(task.workerUuid, result.workerUuid)
        assertEquals(task.categoryCode, result.categoryCode)
        assertEquals(task.status, result.status)
        assertEquals(task.createdDate, result.createdDate)
        assertEquals(task.updatedDate, result.updatedDate)
    }
}
