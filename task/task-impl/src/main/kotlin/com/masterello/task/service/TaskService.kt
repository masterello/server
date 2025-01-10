package com.masterello.task.service

import com.masterello.task.dto.*
import com.masterello.task.entity.UserRating
import com.masterello.task.entity.WorkerRating
import com.masterello.task.exception.BadRequestException
import com.masterello.task.exception.NotFoundException
import com.masterello.task.mapper.TaskMapper
import com.masterello.task.mapper.UserTaskReviewMapper
import com.masterello.task.mapper.WorkerTaskReviewMapper
import com.masterello.task.repository.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaskService(private val taskRepository: TaskRepository,
                  private val workerTaskReviewRepository: WorkerTaskReviewRepository,
                  private val workerRatingRepository: WorkerRatingRepository,
                  private val userRatingRepository: UserRatingRepository,
                  private val userTaskReviewRepository: UserTaskReviewRepository,
                  private val taskMapper: TaskMapper,
                  private val workTaskReviewMapper: WorkerTaskReviewMapper,
                  private val userTaskReviewMapper: UserTaskReviewMapper
) : ReadOnlyTaskService {
    private val log = KotlinLogging.logger {}
    /**
     * Retrieves a task by its unique identifier.
     *
     * This method searches for a task in the database using its UUID. If the task is found,
     * it is mapped to a TaskDto and returned. If no task is found with the given UUID,
     * a `NotFoundException` is thrown. Logs are generated for both the search and the result stages.
     *
     * @param taskUuid the unique identifier of the task to retrieve.
     * @return a TaskDto representing the retrieved task, or null if no task is found.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    override fun getTask(taskUuid: UUID): TaskDto {
        log.info { "Searching for task: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
        if (task.isEmpty) {
            throw NotFoundException("Task not found")
        }
        val taskDto = taskMapper.mapEntityToDto(task.get())
        log.info { "Found a task: $taskDto" }
        return taskDto
    }

    override fun getUserTasks(userUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        val pageable: Pageable = createPageable(taskDtoRequest)
        val taskPage = taskRepository.findAllByUserUuid(userUuid, pageable)
        val tasksDto = taskPage.content.map { taskMapper.mapEntityToDto(it) }
        return PageOfTaskDto(
            totalPages = taskPage.totalPages,
            totalElements = taskPage.totalElements,
            currentPage = taskDtoRequest.page,
            tasks = tasksDto
        )
    }

    override fun getWorkerTasks(workerUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        val pageable: Pageable = createPageable(taskDtoRequest)
        val taskPage = taskRepository.findAllByWorkerUuid(workerUuid, pageable)
        val tasksDto = taskPage.content.map { taskMapper.mapEntityToDto(it) }
        return PageOfTaskDto(
            totalPages = taskPage.totalPages,
            totalElements = taskPage.totalElements,
            currentPage = taskDtoRequest.page,
            tasks = tasksDto
        )
    }

    override fun getTasks(taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        val pageable: Pageable = createPageable(taskDtoRequest)
        val taskPage = taskRepository.findAll(pageable)
        val tasksDto = taskPage.content.map { taskMapper.mapEntityToDto(it) }
        return PageOfTaskDto(
            totalPages = taskPage.totalPages,
            totalElements = taskPage.totalElements,
            currentPage = taskDtoRequest.page,
            tasks = tasksDto
        )
    }

    fun getAmountOfCompletedWorkerTasks(workerUuid: UUID): Long {
        return taskRepository.countByWorkerUuidAndCompletedStatus(workerUuid)
    }

    /**
     * Creates a new task in the system. If worker is not specified, then task was created
     * using user form and all workers can discuss this task with user. If worker is specified,
     * then this task was created using worker page and user is proposing worker to discuss
     * and take this job.
     *
     * @param taskDto the data transfer object representing the task to be created.
     * @return a TaskDto representing the saved task after persistence.
     */
    fun createTask(taskDto: TaskDto): TaskDto {
        log.info { "Creating new task: $taskDto" }

        val task = taskMapper.mapDtoToEntity(taskDto)
        if (task.workerUuid == null) {
            task.status = TaskStatus.NEW
        } else {
            task.status = TaskStatus.ASSIGNED_TO_WORKER
        }
        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Created new task: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Updates a task in the system with the provided data.
     *
     * This method retrieves the existing task by its UUID, updates its fields only if the corresponding
     * fields in the provided `UpdateTaskDto` are non-null, and saves the updated task back to the database
     *
     * @param taskUuid the unique identifier of the task to update.
     * @param taskDto the data transfer object containing the fields to update.
     * @return a TaskDto representing the updated task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun updateTask(taskUuid: UUID, taskDto: UpdateTaskDto): TaskDto {
        log.info { "Updating task with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }

        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        taskDto.categoryUuid?.let { task.categoryUuid = it }
        taskDto.name?.let { task.name = it }
        taskDto.description?.let { task.description = it }

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Updated task: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Assign the task in the system with the provided worker.
     *
     * @param taskUuid the unique identifier of the task to update.
     * @param workerUuid the unique identifier of the worker to be assigned.
     * @return a TaskDto representing the assigned task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun assignTask(taskUuid: UUID, workerUuid: UUID): TaskDto {
        log.info { "Updating task with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if (isValidStatusTransition(task.status, TaskStatus.ASSIGNED_TO_WORKER)) {
            log.info {"Status updated to assigned to worker"}
            task.status = TaskStatus.ASSIGNED_TO_WORKER
        } else {
            throw BadRequestException("Invalid status transition from ${task.status} " +
                    "to ${TaskStatus.ASSIGNED_TO_WORKER})")
        }
        task.workerUuid = workerUuid

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Assigned task: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Unassign the task in the system from worker.
     *
     * @param taskUuid the unique identifier of the task to update.
     * @return a TaskDto representing the unassigned task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun unassignTask(taskUuid: UUID): TaskDto {
        log.info { "Unassigning task from worker with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if (task.status in listOf(TaskStatus.ASSIGNED_TO_WORKER, TaskStatus.NEW)) {
            log.info {"Unassigning task from worker"}
            task.status = TaskStatus.NEW
        } else {
            throw BadRequestException("Task is already in progress, impossible to unassign")
        }
        task.workerUuid = null

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Unassigned task: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Move the task in the system tto in progress. Only assigned worker can move task
     * to this state.
     *
     * @param taskUuid the unique identifier of the task to update.
     * @param workerUuid the unique identifier of the worker to confirm the task.
     * @return a TaskDto representing the in progress task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun confirmTask(taskUuid: UUID, workerUuid: UUID): TaskDto {
        log.info { "Worker is confirming task with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if (isValidStatusTransition(task.status, TaskStatus.IN_PROGRESS)
            && task.workerUuid == workerUuid) {
            log.info {"Status updated to in progress"}
            task.status = TaskStatus.IN_PROGRESS
        } else {
            throw BadRequestException("Invalid task update")
        }
        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Changed task to in progress: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Reassign the task in the system to another worker.
     *
     * @param taskUuid the unique identifier of the task to update.
     * @param workerUuid the unique identifier of the new worker to be assigned.
     * @return a TaskDto representing the reassigned task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun reassignTask(taskUuid: UUID, workerUuid: UUID): TaskDto {
        log.info { "Updating task with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if (task.workerUuid == null) {
            log.info { "no worker is assigned, assigning new" }
            task.status = TaskStatus.ASSIGNED_TO_WORKER
            task.workerUuid = workerUuid
        } else if (task.status in listOf(TaskStatus.ASSIGNED_TO_WORKER, TaskStatus.IN_PROGRESS) &&
            isValidStatusTransition(task.status, TaskStatus.ASSIGNED_TO_WORKER)) {
            log.info { "reassigning task to another worker" }
            task.status = TaskStatus.ASSIGNED_TO_WORKER
            task.workerUuid = workerUuid
        //todo: send a notification to old worker that user removed
        // this task from him and he can left a review
        } else {
            log.error { "Task with uuid is already completed: $taskUuid" }
            throw BadRequestException("Task is already completed")
        }

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Reassigned task: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Complete the task in the system.
     *
     * @param taskUuid the unique identifier of the task to update.
     * @return a TaskDto representing the completion task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun completeTask(taskUuid: UUID): TaskDto {
        log.info { "Completing task with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }
        if (isValidStatusTransition(task.status, TaskStatus.IN_REVIEW)) {
            log.info {"Status updated to in review"}
            task.status = TaskStatus.IN_REVIEW
        } else {
            throw BadRequestException("Invalid status transition from ${task.status} " +
                    "to ${TaskStatus.IN_REVIEW})")
        }
        //TODO: notify user & worker that they can left a review and rate each other

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Completed task: $savedTaskDto" }
        return savedTaskDto
    }

    /**
     * Cancel the task in the system.
     *
     * @param taskUuid the unique identifier of the task to update.
     * @return a TaskDto representing the cancelled task after persistence.
     * @throws NotFoundException if no task is found for the given UUID.
     */
    fun cancelTask(taskUuid: UUID): TaskDto {
        log.info { "Cancelling task with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        task.status = TaskStatus.CANCELLED

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Cancelled task: $savedTaskDto" }
        return savedTaskDto
    }

    fun reviewTaskByWorker(workerReviewDto: WorkerReviewDto): WorkerReviewDto {
        val taskUuid = workerReviewDto.taskUuid
        log.info { "Completing worker task review with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }

        var taskReview = workerTaskReviewRepository.findByTaskUuid(taskUuid)

        if (taskReview == null) {
            taskReview = workTaskReviewMapper.mapDtoToEntity(workerReviewDto)
        } else {
            log.info { "Updating worker task review for task $taskUuid" }
            taskReview.review = workerReviewDto.workerReview
        }
        if (task.workerUuid != workerReviewDto.workerUuid) {
            throw BadRequestException("Task is assigned to another worker")
        }

        var taskRating = workerRatingRepository.findByTaskUuid(taskUuid)

        if (taskRating == null) {
            taskRating = WorkerRating(rating = workerReviewDto.rating,
                taskUuid = workerReviewDto.taskUuid)
        } else {
            log.info { "Updating worker rating for task $taskUuid" }
            taskRating.rating = workerReviewDto.rating
        }

        val workerTaskReview = workerTaskReviewRepository.saveAndFlush(taskReview)
        val workerRating = workerRatingRepository.saveAndFlush(taskRating)

        val userTaskReview = userTaskReviewRepository.findByTaskUuid(taskUuid)
        if (userTaskReview != null) {
            task.status = TaskStatus.DONE
        }
        return workTaskReviewMapper.mapEntityToDto(workerTaskReview,workerRating)
    }

    fun reviewTaskByUser(userReviewDto: UserReviewDto): UserReviewDto {
        val taskUuid = userReviewDto.taskUuid
        log.info { "Completing user task review with uuid: $taskUuid" }

        val task = taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
        if (task.userUuid != userReviewDto.userUuid) {
            throw BadRequestException("Task is assigned to another user")
        }

        var taskReview = userTaskReviewRepository.findByTaskUuid(taskUuid)

        if (taskReview == null) {
            taskReview = userTaskReviewMapper.mapDtoToEntity(userReviewDto)
        } else {
            log.info { "Updating user task review for task $taskUuid" }
            taskReview.review = userReviewDto.userReview
        }

        var taskRating = userRatingRepository.findByTaskUuid(taskUuid)

        if (taskRating == null) {
            taskRating = UserRating(rating = userReviewDto.rating,
                taskUuid = userReviewDto.taskUuid)
        } else {
            log.info { "Updating worker rating for task $taskUuid" }
            taskRating.rating = userReviewDto.rating
        }

        val userTaskReview = userTaskReviewRepository.saveAndFlush(taskReview)
        val userRating = userRatingRepository.saveAndFlush(taskRating)

        val workerTaskReview = workerTaskReviewRepository.findByTaskUuid(taskUuid)
        if (workerTaskReview != null) {
            task.status = TaskStatus.DONE
            taskRepository.saveAndFlush(task)
        }

        return userTaskReviewMapper.mapEntityToDto(userTaskReview, userRating)
    }

    private fun isValidStatusTransition(currentStatus: TaskStatus, newStatus: TaskStatus): Boolean {
        return when (currentStatus) {
            TaskStatus.NEW -> newStatus in listOf(TaskStatus.ASSIGNED_TO_WORKER, TaskStatus.CANCELLED)
            TaskStatus.ASSIGNED_TO_WORKER -> newStatus in listOf(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED)
            TaskStatus.IN_PROGRESS -> newStatus in listOf(TaskStatus.ASSIGNED_TO_WORKER, TaskStatus.IN_REVIEW,
                TaskStatus.CANCELLED)
            TaskStatus.IN_REVIEW -> newStatus == TaskStatus.DONE
            TaskStatus.CANCELLED, TaskStatus.DONE -> false // Can't transition from CANCELLED or DONE
        }
    }

    private fun isEndedTask(currentStatus: TaskStatus): Boolean {
        return currentStatus in listOf(TaskStatus.CANCELLED, TaskStatus.DONE)
    }

    private fun createPageable(taskDtoRequest: TaskDtoRequest): Pageable {
        val sortOrders = taskDtoRequest.sort.fields.map {
            Sort.Order(
                if (taskDtoRequest.sort.order == TaskDtoRequest.SortOrder.ASC) Sort.Direction.ASC else Sort.Direction.DESC,
                it
            )
        }
        val sort = if (sortOrders.isNotEmpty()) Sort.by(sortOrders) else Sort.unsorted()
        return PageRequest.of(taskDtoRequest.page, taskDtoRequest.pageSize, sort)
    }
}