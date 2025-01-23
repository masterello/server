package com.masterello.task.service

import com.masterello.auth.data.AuthData
import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.task.dto.ReviewerType
import com.masterello.task.dto.TaskDtoRequest
import com.masterello.task.dto.TaskDtoRequest.SortOrder
import com.masterello.task.dto.TaskStatus
import com.masterello.task.exception.BadRequestException
import com.masterello.task.exception.NotFoundException
import com.masterello.task.mapper.TaskMapper
import com.masterello.task.mapper.TaskReviewMapper
import com.masterello.task.repository.TaskRepository
import com.masterello.task.repository.TaskReviewRepository
import com.masterello.task.repository.UserRatingRepository
import com.masterello.task.repository.WorkerRatingRepository
import com.masterello.task.test.WorkerTest
import com.masterello.task.util.TestDataUtils
import com.masterello.worker.service.ReadOnlyWorkerService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {
    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var taskReviewRepository: TaskReviewRepository

    @Mock
    private lateinit var workerRatingRepository: WorkerRatingRepository

    @Mock
    private lateinit var userRatingRepository: UserRatingRepository

    @Mock
    private lateinit var taskMapper: TaskMapper

    @Mock
    private lateinit var taskReviewMapper: TaskReviewMapper

    @Mock
    private lateinit var workerService: ReadOnlyWorkerService

    @Mock
    private lateinit var securityContext: SecurityContext

    @Mock
    private lateinit var authentication: MasterelloAuthentication

    @Mock
    private lateinit var authData: AuthData

    @InjectMocks
    private lateinit var taskService: TaskService

    @BeforeEach
    fun setup() {
        SecurityContextHolder.setContext(securityContext)
        `when`(securityContext.authentication).thenReturn(authentication)
    }

    @Test
    fun `getTask should return TaskDto when task is found`() {
        val taskUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid)
        val taskDto = TestDataUtils.createTaskDto(uuid = taskUuid)
        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskMapper.mapEntityToDto(task)).thenReturn(taskDto)

        val result = taskService.getTask(taskUuid)

        assertEquals(taskDto, result)
        verify(taskRepository).findById(taskUuid)
        verify(taskMapper).mapEntityToDto(task)
    }

    @Test
    fun `getTask should return null when task is not found`() {
        val taskUuid = UUID.randomUUID()
        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val result = taskService.getTask(taskUuid)

        assertNull(result)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `getUserTasks should return a page of TaskDto`() {
        val userUuid = UUID.randomUUID()
        val taskDtoRequest = TaskDtoRequest(page = 1, pageSize = 5,
            sort = TaskDtoRequest.Sort(order = SortOrder.ASC, fields = listOf("createdDate")))
        val task = TestDataUtils.createTask(userUuid = userUuid)
        val pageable = PageRequest.of(taskDtoRequest.page, taskDtoRequest.pageSize, Sort.by(Sort.Order.asc("createdDate")))
        `when`(taskRepository.findAllByUserUuid(userUuid, pageable)).thenReturn(PageImpl(listOf(task)))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.getUserTasks(userUuid, taskDtoRequest)

        assertNotNull(result)
        assertEquals(1, result.tasks.size)
        verify(taskRepository).findAllByUserUuid(userUuid, pageable)
    }

    @Test
    fun `getWorkerTasks should return a page of TaskDto`() {
        val workerUuid = UUID.randomUUID()
        val taskDtoRequest = TaskDtoRequest(page = 1, pageSize = 5,
            sort = TaskDtoRequest.Sort(order = SortOrder.ASC, fields = listOf("createdDate")))
        val task = TestDataUtils.createTask(userUuid = workerUuid)
        val pageable = PageRequest.of(taskDtoRequest.page, taskDtoRequest.pageSize, Sort.by(Sort.Order.asc("createdDate")))
        `when`(taskRepository.findAllByWorkerUuid(workerUuid, pageable)).thenReturn(PageImpl(listOf(task)))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)

        val result = taskService.getWorkerTasks(workerUuid, taskDtoRequest)

        assertNotNull(result)
        assertEquals(1, result.tasks.size)
        verify(taskRepository).findAllByWorkerUuid(workerUuid, pageable)
    }

    @Test
    fun `getOpenTasksByCategoryCode should throw exception when categoryCodes is empty`() {
        val taskDtoRequest = TaskDtoRequest(categoryCodes = emptyList())

        val exception = assertThrows<BadRequestException> {
            taskService.getOpenTasksByCategoryCode(taskDtoRequest)
        }

        assertEquals("Categories are not provided, provide at least 1 category", exception.message)
    }

    @Test
    fun `getOpenTasksByCategoryCode should return a page of TaskDto`() {
        val categoryCodes = listOf(1, 2)
        val taskDtoRequest = TaskDtoRequest(page = 1, pageSize = 5, categoryCodes = categoryCodes)
        val task = TestDataUtils.createTask(categoryCode = 1)
        val pageable = PageRequest.of(taskDtoRequest.page, taskDtoRequest.pageSize, Sort.by(Sort.Order.asc("createdDate")))
        `when`(taskRepository.findNewTasksByCategoryCode(categoryCodes, pageable))
            .thenReturn(PageImpl(listOf(task)))

        val result = taskService.getOpenTasksByCategoryCode(taskDtoRequest)

        assertNotNull(result)
        assertEquals(1, result.tasks.size)
        verify(taskRepository).findNewTasksByCategoryCode(categoryCodes, pageable)
    }

    @Test
    fun `getAmountOfCompletedWorkerTasks should return the count of completed tasks`() {
        val workerUuid = UUID.randomUUID()
        val completedTaskCount = 5L
        `when`(taskRepository.countByWorkerUuidAndCompletedStatus(workerUuid)).thenReturn(completedTaskCount)

        val result = taskService.getAmountOfCompletedWorkerTasks(workerUuid)

        assertEquals(completedTaskCount, result)
        verify(taskRepository).countByWorkerUuidAndCompletedStatus(workerUuid)
    }

    @Test
    fun `createTask should create task with NEW status when workerUuid is null`() {
        val userUuid = UUID.randomUUID()
        val taskDto = TestDataUtils.createTaskDto(workerUuid = null, userUuid = userUuid)
        val task = TestDataUtils.createTask(workerUuid = null, userUuid = userUuid)

        `when`(taskMapper.mapDtoToEntity(taskDto)).thenReturn(task)
        `when`(taskRepository.saveAndFlush(task)).thenReturn(task)
        `when`(taskMapper.mapEntityToDto(task)).thenReturn(taskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.createTask(taskDto)

        assertNotNull(result)
        assertEquals(TaskStatus.NEW, result.status)
        verify(taskMapper).mapDtoToEntity(taskDto)
        verify(taskRepository).saveAndFlush(task)
        verify(taskMapper).mapEntityToDto(task)
    }

    @Test
    fun `createTask should create task with ASSIGNED_TO_WORKER status when workerUuid is not null and worker exists`() {
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val taskDto = TestDataUtils.createTaskDto(workerUuid = workerUuid, userUuid = userUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val task = TestDataUtils.createTask(workerUuid = workerUuid, userUuid = userUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val worker = WorkerTest()

        `when`(taskMapper.mapDtoToEntity(taskDto)).thenReturn(task)
        `when`(workerService.getWorkerInfo(workerUuid)).thenReturn(Optional.of(worker))
        `when`(taskRepository.saveAndFlush(task)).thenReturn(task)
        `when`(taskMapper.mapEntityToDto(task)).thenReturn(taskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.createTask(taskDto)

        assertNotNull(result)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, result.status)
        verify(taskMapper).mapDtoToEntity(taskDto)
        verify(workerService).getWorkerInfo(workerUuid)
        verify(taskRepository).saveAndFlush(task)
        verify(taskMapper).mapEntityToDto(task)
    }

    @Test
    fun `createTask should throw BadRequestException when workerUuid is not null and worker does not exist`() {
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val taskDto = TestDataUtils.createTaskDto(workerUuid = workerUuid, userUuid = userUuid)
        val task = TestDataUtils.createTask(workerUuid = workerUuid, userUuid = userUuid)

        `when`(workerService.getWorkerInfo(workerUuid)).thenReturn(Optional.empty())
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)
        `when`(taskMapper.mapDtoToEntity(taskDto)).thenReturn(task)

        val exception = assertThrows<BadRequestException> {
            taskService.createTask(taskDto)
        }

        assertEquals("Worker not found", exception.message)
        verify(taskMapper).mapDtoToEntity(taskDto)
        verify(workerService).getWorkerInfo(workerUuid)
        verify(taskRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `createTask should throw BadRequestException when userUuid from context is not same as from dto`() {
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val taskDto = TestDataUtils.createTaskDto(workerUuid = workerUuid, userUuid = userUuid)
        val task = TestDataUtils.createTask(workerUuid = workerUuid, userUuid = userUuid)

        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())
        `when`(taskMapper.mapDtoToEntity(taskDto)).thenReturn(task)

        val exception = assertThrows<BadRequestException> {
            taskService.createTask(taskDto)
        }

        assertEquals("Attempting to assign task to a wrong user", exception.message)
        verify(taskMapper).mapDtoToEntity(taskDto)
        verify(taskRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `updateTask should update task fields and return updated TaskDto`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val updateTaskDto = TestDataUtils.updateTaskDto(name = "Updated Name", categoryCode = 5)
        val updatedTask = task.copy(name = "Updated Name", categoryCode = 5)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, name = "Updated Name", categoryCode = 5)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.updateTask(taskUuid, updateTaskDto)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        verify(taskRepository).findById(taskUuid)
        verify(taskRepository).saveAndFlush(task)
        verify(taskMapper).mapEntityToDto(updatedTask)
    }

    @Test
    fun `updateTask should throw exception when task is not found`() {
        val taskUuid = UUID.randomUUID()
        val updateTaskDto = TestDataUtils.updateTaskDto()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            taskService.updateTask(taskUuid, updateTaskDto)
        }

        assertEquals("Task not found", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `updateTask should throw exception when task is already cancelled or done`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.DONE)
        val updateTaskDto = TestDataUtils.updateTaskDto()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.updateTask(taskUuid, updateTaskDto)
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `updateTask should throw exception when task user is not owner`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid)
        val updateTaskDto = TestDataUtils.updateTaskDto()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.updateTask(taskUuid, updateTaskDto)
        }

        assertEquals("Invalid user request attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `assignTask should assign a task to worker and update status`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.NEW)
        val updatedTask = task.copy(status = TaskStatus.ASSIGNED_TO_WORKER, workerUuid = workerUuid)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, status = TaskStatus.ASSIGNED_TO_WORKER,
            workerUuid = workerUuid)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)

        val result = taskService.assignTask(taskUuid, workerUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertEquals(workerUuid, updatedTask.workerUuid)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, updatedTask.status)
        verify(taskRepository).findById(taskUuid)
        verify(taskRepository).saveAndFlush(task)
        verify(taskMapper).mapEntityToDto(updatedTask)
    }

    @Test
    fun `assignTask should throw exception if task is already ended`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.assignTask(taskUuid, UUID.randomUUID())
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `assignTask should throw exception when task is not found`() {
        val taskUuid = UUID.randomUUID()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            taskService.assignTask(taskUuid, UUID.randomUUID())
        }

        assertEquals("Task not found", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `assignTask should throw exception if when task user is not owner`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.assignTask(taskUuid, UUID.randomUUID())
        }

        assertEquals("Invalid user request attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `assignTask should throw exception if task already has a worker`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, workerUuid = UUID.randomUUID(), status = TaskStatus.NEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.assignTask(taskUuid, UUID.randomUUID())
        }

        assertEquals("Task already has a worker", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `unassignTask should unassign worker and set status to NEW`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val updatedTask = task.copy(workerUuid = null, status = TaskStatus.NEW)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, status = TaskStatus.NEW, workerUuid = null)

        `when`(authentication.details).thenReturn(authData)
        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)

        val result = taskService.unassignTask(taskUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertNull(updatedTask.workerUuid)
        assertEquals(TaskStatus.NEW, updatedTask.status)
        verify(taskRepository).findById(taskUuid)
        verify(taskRepository).saveAndFlush(task)
        verify(taskMapper).mapEntityToDto(updatedTask)
    }

    @Test
    fun `unassignTask should throw exception if task has no worker`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, workerUuid = null, status = TaskStatus.NEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)
        val exception = assertThrows<BadRequestException> {
            taskService.unassignTask(taskUuid)
        }

        assertEquals("Task has no worker", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `unassignTask should throw exception when task is not found`() {
        val taskUuid = UUID.randomUUID()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            taskService.unassignTask(taskUuid)
        }

        assertEquals("Task not found", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `unassignTask should throw exception if task is already ended`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid,  status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.unassignTask(taskUuid)
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `unassignTask should throw exception if when task worker is not owner`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, workerUuid = workerUuid,
            status = TaskStatus.ASSIGNED_TO_WORKER)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.unassignTask(taskUuid)
        }

        assertEquals("Invalid worker request attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }


    @Test
    fun `unassignTask should throw exception if task is in progress`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.IN_PROGRESS)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.unassignTask(taskUuid)
        }

        assertEquals("Task is already in progress, impossible to unassign", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `confirmTask should update status to IN_PROGRESS if valid`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val updatedTask = task.copy(status = TaskStatus.IN_PROGRESS)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, status = TaskStatus.IN_PROGRESS, workerUuid = workerUuid)

        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)
        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)

        val result = taskService.confirmTask(taskUuid, workerUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.status)
        verify(taskRepository).findById(taskUuid)
        verify(taskRepository).saveAndFlush(task)
        verify(taskMapper).mapEntityToDto(updatedTask)
    }

    @Test
    fun `confirmTask should throw exception if task status is ended`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.confirmTask(taskUuid, workerUuid)
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `confirmTask should throw exception for invalid status transition`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.NEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(workerUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.confirmTask(taskUuid, workerUuid)
        }

        assertEquals("Invalid task update", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `confirmTask should throw exception if workerUuid is not the owner`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.confirmTask(taskUuid, workerUuid)
        }

        assertEquals("Invalid worker request attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `confirmTask should throw exception when task is not found`() {
        val taskUuid = UUID.randomUUID()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            taskService.confirmTask(taskUuid, UUID.randomUUID())
        }

        assertEquals("Task not found", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `reassignTask should assign a new worker if no worker was previously assigned`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid,userUuid = userUuid, workerUuid = null, status = TaskStatus.NEW)
        val updatedTask = task.copy(workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)

        val result = taskService.reassignTask(taskUuid, workerUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, task.status)
        verify(taskRepository).saveAndFlush(task)
    }

    @Test
    fun `reassignTask should throw exception when task is not found`() {
        val taskUuid = UUID.randomUUID()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            taskService.confirmTask(taskUuid, UUID.randomUUID())
        }

        assertEquals("Task not found", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `reassignTask should return task if worker is already assigned`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, userUuid = userUuid, workerUuid = workerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskMapper.mapEntityToDto(task)).thenReturn(expectedTaskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.reassignTask(taskUuid, workerUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        verify(taskMapper).mapEntityToDto(task)
    }

    @Test
    fun `reassignTask should throw exception if task status is ended`() {
        val taskUuid = UUID.randomUUID()
        val workerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid,userUuid = userUuid, workerUuid = UUID.randomUUID(), status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.reassignTask(taskUuid, workerUuid)
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `reassignTask should update worker if valid reassignment`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val currentWorkerUuid = UUID.randomUUID()
        val newWorkerUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid , workerUuid = currentWorkerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)
        val updatedTask = task.copy(workerUuid = newWorkerUuid)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, workerUuid = newWorkerUuid, status = TaskStatus.ASSIGNED_TO_WORKER)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.reassignTask(taskUuid, newWorkerUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertEquals(newWorkerUuid, task.workerUuid)
        verify(taskRepository).saveAndFlush(task)
    }

    @Test
    fun `completeTask should update status to IN_REVIEW for valid task`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.IN_PROGRESS)
        val updatedTask = task.copy(status = TaskStatus.IN_REVIEW)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, status = TaskStatus.IN_REVIEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskRepository.saveAndFlush(task)).thenReturn(updatedTask)
        `when`(taskMapper.mapEntityToDto(updatedTask)).thenReturn(expectedTaskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.completeTask(taskUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertEquals(TaskStatus.IN_REVIEW, task.status)
        verify(taskRepository).saveAndFlush(task)
    }

    @Test
    fun `completeTask should throw exception when task is not found`() {
        val taskUuid = UUID.randomUUID()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            taskService.completeTask(taskUuid)
        }

        assertEquals("Task not found", exception.message)
        verify(taskRepository).findById(taskUuid)
        verifyNoInteractions(taskMapper)
    }

    @Test
    fun `completeTask should throw exception if task is already ended`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.completeTask(taskUuid)
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `completeTask should throw exception for invalid status transition`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid,userUuid = userUuid, status = TaskStatus.NEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.completeTask(taskUuid)
        }

        assertEquals("Invalid status transition from NEW to IN_REVIEW)", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `completeTask should throw exception if when task user is not owner`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.completeTask(taskUuid)
        }

        assertEquals("Invalid update attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `cancelTask should update task status to CANCELLED`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.IN_PROGRESS)
        val cancelledTask = task.copy(status = TaskStatus.CANCELLED)
        val expectedTaskDto = TestDataUtils.createTaskDto(uuid = taskUuid, status = TaskStatus.CANCELLED)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskRepository.saveAndFlush(task)).thenReturn(cancelledTask)
        `when`(taskMapper.mapEntityToDto(cancelledTask)).thenReturn(expectedTaskDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.cancelTask(taskUuid)

        assertNotNull(result)
        assertEquals(expectedTaskDto, result)
        assertEquals(TaskStatus.CANCELLED, task.status)
        verify(taskRepository).saveAndFlush(task)
    }

    @Test
    fun `cancelTask should throw exception if task is already ended`() {
        val taskUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.DONE)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.cancelTask(taskUuid)
        }

        assertEquals("Task is already cancelled or done", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `cancelTask should check task ownership`() {
        val taskUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, status = TaskStatus.IN_PROGRESS)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.cancelTask(taskUuid)
        }

        assertEquals("Invalid user request attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `reviewTaskByWorker should throw exception if reviewer type is USER`() {
        val reviewDto = TestDataUtils.getReviewDto(reviewerType = ReviewerType.USER)
        val userUuid = UUID.randomUUID()
        val taskUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.IN_REVIEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.reviewTaskByWorker(reviewDto)
        }

        assertEquals("Invalid reviewer type", exception.message)
    }

    @Test
    fun `reviewTaskByUser should throw exception if reviewer type is WORKER`() {
        val reviewDto = TestDataUtils.getReviewDto()
        val userUuid = UUID.randomUUID()
        val taskUuid = UUID.randomUUID()
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.IN_REVIEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val exception = assertThrows<BadRequestException> {
            taskService.reviewTaskByUser(reviewDto)
        }

        assertEquals("Invalid reviewer type", exception.message)
    }

    @Test
    fun `reviewTaskByWorker should throw exception when task worker is not owner`() {
        val workerUuid = UUID.randomUUID()
        val taskUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid)
        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = workerUuid, status = TaskStatus.IN_REVIEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.reviewTaskByWorker(reviewDto)
        }

        assertEquals("Invalid update attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `reviewTaskByUser should throw exception when task user is not owner`() {
        val userUuid = UUID.randomUUID()
        val taskUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerType = ReviewerType.USER)
        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.IN_REVIEW)

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(UUID.randomUUID())

        val exception = assertThrows<BadRequestException> {
            taskService.reviewTaskByUser(reviewDto)
        }

        assertEquals("Invalid update attempt", exception.message)
        verify(taskRepository).findById(taskUuid)
    }

    @Test
    fun `reviewTaskByWorker should update review when valid`() {
        val taskUuid = UUID.randomUUID()
        val reviewerUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = reviewerUuid)
        val review = TestDataUtils.getReview()

        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = userUuid, status = TaskStatus.IN_REVIEW, workerUuid = reviewerUuid)
        val taskReview = TestDataUtils.getWorkerReview()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskReviewRepository.findByTaskUuidAndReviewerUuid(taskUuid, reviewerUuid)).thenReturn(review)
        `when`(taskReviewRepository.saveAndFlush(review)).thenReturn(review)
        `when`(workerRatingRepository.findByTaskUuid(taskUuid)).thenReturn(taskReview)
        `when`(workerRatingRepository.saveAndFlush(taskReview)).thenReturn(taskReview)
        `when`(taskReviewRepository.findByTaskUuid(taskUuid)).thenReturn(listOf(review))
        `when`(taskReviewMapper.mapEntityToDto(review, 5)).thenReturn(reviewDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(userUuid)

        val result = taskService.reviewTaskByWorker(reviewDto)

        assertNotNull(result)
        assertEquals(5, result.rating)
        assertEquals("Good job!", result.review)
    }

    @Test
    fun `reviewTaskByUser should update review when valid`() {
        val taskUuid = UUID.randomUUID()
        val reviewerUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = reviewerUuid, reviewerType = ReviewerType.USER)
        val review = TestDataUtils.getReview()

        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = reviewerUuid, status = TaskStatus.IN_REVIEW, workerUuid = reviewerUuid)
        val taskReview = TestDataUtils.getUserReview()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskReviewRepository.findByTaskUuidAndReviewerUuid(taskUuid, reviewerUuid)).thenReturn(review)
        `when`(taskReviewRepository.saveAndFlush(review)).thenReturn(review)
        `when`(userRatingRepository.findByTaskUuid(taskUuid)).thenReturn(taskReview)
        `when`(userRatingRepository.saveAndFlush(taskReview)).thenReturn(taskReview)
        `when`(taskReviewRepository.findByTaskUuid(taskUuid)).thenReturn(listOf(review))
        `when`(taskReviewMapper.mapEntityToDto(review, 5)).thenReturn(reviewDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(reviewerUuid)

        val result = taskService.reviewTaskByUser(reviewDto)

        assertNotNull(result)
        assertEquals(5, result.rating)
        assertEquals("Good job!", result.review)
    }

    @Test
    fun `reviewTaskByWorker should save review when valid`() {
        val taskUuid = UUID.randomUUID()
        val reviewerUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = reviewerUuid)
        val review = TestDataUtils.getReview()

        val task = TestDataUtils.createTask(uuid = taskUuid, workerUuid = reviewerUuid, status = TaskStatus.IN_REVIEW)
        val taskReview = TestDataUtils.getWorkerReview()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskReviewRepository.findByTaskUuidAndReviewerUuid(taskUuid, reviewerUuid)).thenReturn(null)
        `when`(taskReviewMapper.mapDtoToEntity(reviewDto)).thenReturn(review)
        `when`(taskReviewRepository.saveAndFlush(review)).thenReturn(review)
        `when`(workerRatingRepository.findByTaskUuid(taskUuid)).thenReturn(null)
        `when`(workerRatingRepository.saveAndFlush(any())).thenReturn(taskReview)
        `when`(taskReviewRepository.findByTaskUuid(taskUuid)).thenReturn(listOf(review))
        `when`(taskReviewMapper.mapEntityToDto(review, 5)).thenReturn(reviewDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(reviewerUuid)

        val result = taskService.reviewTaskByWorker(reviewDto)

        assertNotNull(result)
        assertEquals(5, result.rating)
        assertEquals("Good job!", result.review)
    }


    @Test
    fun `reviewTaskByUser should save review when valid`() {
        val taskUuid = UUID.randomUUID()
        val reviewerUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = reviewerUuid, reviewerType = ReviewerType.USER)
        val review = TestDataUtils.getReview()

        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = reviewerUuid, status = TaskStatus.IN_REVIEW, workerUuid = reviewerUuid)
        val taskReview = TestDataUtils.getUserReview()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskReviewRepository.findByTaskUuidAndReviewerUuid(taskUuid, reviewerUuid)).thenReturn(null)
        `when`(taskReviewMapper.mapDtoToEntity(reviewDto)).thenReturn(review)
        `when`(taskReviewRepository.saveAndFlush(review)).thenReturn(review)
        `when`(userRatingRepository.findByTaskUuid(taskUuid)).thenReturn(null)
        `when`(userRatingRepository.saveAndFlush(any())).thenReturn(taskReview)
        `when`(taskReviewRepository.findByTaskUuid(taskUuid)).thenReturn(listOf(review))
        `when`(taskReviewMapper.mapEntityToDto(review, 5)).thenReturn(reviewDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(reviewerUuid)

        val result = taskService.reviewTaskByUser(reviewDto)

        assertNotNull(result)
        assertEquals(5, result.rating)
        assertEquals("Good job!", result.review)
    }

    @Test
    fun `reviewTaskByUser should save review when valid and move task to done`() {
        val taskUuid = UUID.randomUUID()
        val reviewerUuid = UUID.randomUUID()
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = taskUuid, reviewerUuid = reviewerUuid, reviewerType = ReviewerType.USER)
        val review = TestDataUtils.getReview()
        val review2 = TestDataUtils.getReview()

        val task = TestDataUtils.createTask(uuid = taskUuid, userUuid = reviewerUuid, status = TaskStatus.IN_REVIEW, workerUuid = reviewerUuid)
        val taskReview = TestDataUtils.getUserReview()

        `when`(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task))
        `when`(taskReviewRepository.findByTaskUuidAndReviewerUuid(taskUuid, reviewerUuid)).thenReturn(null)
        `when`(taskReviewMapper.mapDtoToEntity(reviewDto)).thenReturn(review)
        `when`(taskReviewRepository.saveAndFlush(review)).thenReturn(review)
        `when`(userRatingRepository.findByTaskUuid(taskUuid)).thenReturn(null)
        `when`(userRatingRepository.saveAndFlush(any())).thenReturn(taskReview)
        `when`(taskReviewRepository.findByTaskUuid(taskUuid)).thenReturn(listOf(review, review2))
        `when`(taskReviewMapper.mapEntityToDto(review, 5)).thenReturn(reviewDto)
        `when`(authentication.details).thenReturn(authData)
        `when`(authData.userId).thenReturn(reviewerUuid)

        val result = taskService.reviewTaskByUser(reviewDto)

        assertNotNull(result)
        assertEquals(5, result.rating)
        assertEquals("Good job!", result.review)

        verify(taskRepository).saveAndFlush(any())
    }
}
