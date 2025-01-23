package com.masterello.task.service

import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.task.dto.*
import com.masterello.task.entity.BaseRating
import com.masterello.task.entity.Task
import com.masterello.task.entity.UserRating
import com.masterello.task.entity.WorkerRating
import com.masterello.task.exception.BadRequestException
import com.masterello.task.exception.NotFoundException
import com.masterello.task.mapper.TaskMapper
import com.masterello.task.mapper.TaskReviewMapper
import com.masterello.task.repository.*
import com.masterello.worker.service.ReadOnlyWorkerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaskService(private val taskRepository: TaskRepository,
                  private val taskReviewRepository: TaskReviewRepository,
                  private val workerRatingRepository: WorkerRatingRepository,
                  private val userRatingRepository: UserRatingRepository,
                  private val taskMapper: TaskMapper,
                  private val taskReviewMapper: TaskReviewMapper,
                  private val workerService: ReadOnlyWorkerService,
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
    override fun getTask(taskUuid: UUID): TaskDto? {
        log.info { "Searching for task: $taskUuid" }
        return taskRepository.findById(taskUuid)
            .map { taskMapper.mapEntityToDto(it) }
            .orElse(null)
    }

    override fun getUserTasks(userUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        checkUserOwnership(userUuid)
        val tasks = taskRepository.findAllByUserUuid(userUuid, createPageable(taskDtoRequest))
        return createTaskPage(tasks, taskDtoRequest.page)
    }

    override fun getWorkerTasks(workerUuid: UUID, taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        checkWorkerOwnership(workerUuid)
        val tasks = taskRepository.findAllByWorkerUuid(workerUuid, createPageable(taskDtoRequest))
        return createTaskPage(tasks, taskDtoRequest.page)
    }

    override fun getTasks(taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        val tasks = taskRepository.findAll(createPageable(taskDtoRequest))
        return createTaskPage(tasks, taskDtoRequest.page)
    }

    override fun getOpenTasksByCategoryCode(taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        if (taskDtoRequest.categoryCodes.isEmpty()) {
            throw BadRequestException("Categories are not provided, provide at least 1 category")
        }
        val tasks = taskRepository.findNewTasksByCategoryCode(taskDtoRequest.categoryCodes,
            createPageable(taskDtoRequest))
        return createTaskPage(tasks, taskDtoRequest.page)
    }

    fun getAmountOfCompletedWorkerTasks(workerUuid: UUID): Long =
        taskRepository.countByWorkerUuidAndCompletedStatus(workerUuid)

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
        if (task.userUuid != getIdFromContext()) {
            throw BadRequestException("Attempting to assign task to a wrong user")
        }
        if (task.workerUuid == null) {
            task.status = TaskStatus.NEW
        } else if (workerService.getWorkerInfo(task.workerUuid).isPresent) {
            task.status = TaskStatus.ASSIGNED_TO_WORKER
        } else {
            throw BadRequestException("Worker not found")
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

        val task = getTaskOrThrow(taskUuid)
        checkUserOwnership(task.userUuid)

        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        taskDto.categoryCode?.let { task.categoryCode = it }
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

        val task = getTaskOrThrow(taskUuid)
        checkUserOwnership(task.userUuid)

        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if(task.workerUuid != null) {
            throw BadRequestException("Task already has a worker")
        }

        if (isValidStatusTransition(task.status, TaskStatus.ASSIGNED_TO_WORKER)) {
            log.info {"Status updated to assigned to worker"}
            task.status = TaskStatus.ASSIGNED_TO_WORKER
        } else {
            throw BadRequestException("Invalid status transition from ${task.status} " +
                    "to ${TaskStatus.ASSIGNED_TO_WORKER}")
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

        val task = getTaskOrThrow(taskUuid)

        if (task.workerUuid == null) {
            throw BadRequestException("Task has no worker")
        } else {
            checkWorkerOwnership(task.workerUuid)
        }

        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if (isValidStatusTransition(task.status, TaskStatus.NEW)) {
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

        val task = getTaskOrThrow(taskUuid)
        checkWorkerOwnership(task.workerUuid)

        if (task.workerUuid == null) {
            throw BadRequestException("Task has no worker")
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

        val task = getTaskOrThrow(taskUuid)
        checkUserOwnership(task.userUuid)

        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        if (task.workerUuid == workerUuid) {
            log.info { "current worker is already assigned, nothing to do" }
            return taskMapper.mapEntityToDto(task)
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
            log.error { "Task with uuid is new or already completed: $taskUuid" }
            throw BadRequestException("Task is new already completed")
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

        val task = getTaskOrThrow(taskUuid)
        checkTaskOwnership(task)

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

        val task = getTaskOrThrow(taskUuid)
        checkUserOwnership(task.userUuid)

        if (isEndedTask(task.status)) {
            throw BadRequestException("Task is already cancelled or done")
        }

        task.status = TaskStatus.CANCELLED

        val savedTask = taskRepository.saveAndFlush(task)
        val savedTaskDto = taskMapper.mapEntityToDto(savedTask)

        log.info { "Cancelled task: $savedTaskDto" }
        return savedTaskDto
    }

    fun reviewTaskByWorker(reviewDto: ReviewDto): ReviewDto {
        if (reviewDto.reviewerType == ReviewerType.USER) {
            throw BadRequestException("Invalid reviewer type")
        }
        return reviewTask(reviewDto, true)
    }

    fun reviewTaskByUser(reviewDto: ReviewDto): ReviewDto {
        if (reviewDto.reviewerType == ReviewerType.WORKER) {
            throw BadRequestException("Invalid reviewer type")
        }
        return reviewTask(reviewDto, false)
    }

    private fun isValidStatusTransition(currentStatus: TaskStatus, newStatus: TaskStatus): Boolean {
        return when (currentStatus) {
            TaskStatus.NEW -> newStatus in listOf(TaskStatus.ASSIGNED_TO_WORKER, TaskStatus.CANCELLED)
            TaskStatus.ASSIGNED_TO_WORKER -> newStatus in listOf(TaskStatus.NEW, TaskStatus.ASSIGNED_TO_WORKER,
                TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED)
            TaskStatus.IN_PROGRESS -> newStatus in listOf(TaskStatus.ASSIGNED_TO_WORKER, TaskStatus.IN_REVIEW,
                TaskStatus.CANCELLED)
            TaskStatus.IN_REVIEW -> newStatus == TaskStatus.DONE
            TaskStatus.CANCELLED, TaskStatus.DONE -> false // Can't transition from CANCELLED or DONE
        }
    }

    private fun isEndedTask(currentStatus: TaskStatus): Boolean {
        return currentStatus in listOf(TaskStatus.IN_REVIEW, TaskStatus.CANCELLED, TaskStatus.DONE)
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

    private fun createTaskPage(tasks: Page<Task>, currentPage: Int ): PageOfTaskDto {
        return PageOfTaskDto(
                totalPages = tasks.totalPages,
        totalElements = tasks.totalElements,
        currentPage = currentPage,
        tasks = tasks.content.map(taskMapper::mapEntityToDto)
        )
    }

    private fun getTaskOrThrow(taskUuid: UUID): Task {
        return taskRepository.findById(taskUuid)
            .orElseThrow {
                log.error { "Task not found for uuid: $taskUuid" }
                NotFoundException("Task not found")
            }
    }

    private fun reviewTask(reviewDto: ReviewDto, isWorker: Boolean): ReviewDto {
        val taskUuid = reviewDto.taskUuid
        log.info { "Completing task review with uuid: $taskUuid by ${reviewDto.reviewerType}" }

        val task = getTaskOrThrow(taskUuid)
        checkTaskOwnership(task)

        if (isWorker) {
            if (task.workerUuid != reviewDto.reviewerUuid) {
                throw BadRequestException("Task is assigned to another worker")
            }
        } else {
            if (task.userUuid != reviewDto.reviewerUuid) {
                throw BadRequestException("Task is assigned to another user")
            }
        }

        var taskReview = taskReviewRepository.findByTaskUuidAndReviewerUuid(taskUuid, reviewDto.reviewerUuid)

        if (taskReview == null) {
            taskReview = taskReviewMapper.mapDtoToEntity(reviewDto)
        } else {
            log.info { "Updating user task review for task $taskUuid" }
            taskReview.review = reviewDto.review
        }
        val updatedTaskReview = taskReviewRepository.saveAndFlush(taskReview)

        val taskReviews = taskReviewRepository.findByTaskUuid(taskUuid)

        val savedRating: BaseRating = if (isWorker) {
            makeWorkerRating(taskUuid, reviewDto)
        } else {
            makeUserRating(taskUuid, reviewDto)
        }

        if (taskReviews.size >= 2) {
            task.status = TaskStatus.DONE
            taskRepository.saveAndFlush(task)
        }
        return taskReviewMapper.mapEntityToDto(updatedTaskReview, savedRating.rating)
    }

    private fun makeWorkerRating(taskUuid: UUID, reviewDto: ReviewDto) : WorkerRating {
        var taskRating = workerRatingRepository.findByTaskUuid(taskUuid)

        if (taskRating == null) {
            taskRating = WorkerRating(rating = reviewDto.rating,
                taskUuid = reviewDto.taskUuid)
        } else {
            log.info { "Updating worker rating for task $taskUuid" }
            taskRating.rating = reviewDto.rating
        }

        return workerRatingRepository.saveAndFlush(taskRating)
    }

    private fun makeUserRating(taskUuid: UUID, reviewDto: ReviewDto) : UserRating {
        var taskRating = userRatingRepository.findByTaskUuid(taskUuid)

        if (taskRating == null) {
            taskRating = UserRating(rating = reviewDto.rating,
                taskUuid = reviewDto.taskUuid)
        } else {
            log.info { "Updating user rating for task $taskUuid" }
            taskRating.rating = reviewDto.rating
        }

        return userRatingRepository.saveAndFlush(taskRating)
    }

    private fun getIdFromContext(): UUID {
        val securityContext = SecurityContextHolder.getContext()
        val tokenData = (securityContext.authentication as? MasterelloAuthentication)?.details
            ?: throw IllegalStateException("Authentication is not of type MasterelloAuthentication")

        return tokenData.userId
    }

    private fun checkTaskOwnership(task: Task) {
        val uuid = getIdFromContext()
        if (task.userUuid != uuid && task.workerUuid != uuid) {
            throw BadRequestException("Invalid update attempt")
        }
    }

    private fun checkUserOwnership(userUuid: UUID) {
        val uuid = getIdFromContext()
        if (userUuid != uuid) {
            throw BadRequestException("Invalid user request attempt")
        }
    }

    private fun checkWorkerOwnership(workerUuid: UUID?) {
        val uuid = getIdFromContext()
        if (workerUuid != uuid) {
            throw BadRequestException("Invalid worker request attempt")
        }
    }
}