package com.masterello.task.controller

import com.masterello.auth.data.AuthZRole
import com.masterello.commons.security.validation.AuthZRule
import com.masterello.commons.security.validation.AuthZRules
import com.masterello.commons.security.validation.OwnerId
import com.masterello.task.dto.*
import com.masterello.task.exception.NotFoundException
import com.masterello.task.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task", description = "API for managing tasks")
class TaskController {

    @Autowired
    private lateinit var taskService: TaskService

    /**
     * Retrieve task by identifier
     * @param taskUuid the identifier of the task
     * @return Stored task in the system
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER, AuthZRole.WORKER]),
    )
    @Operation(summary = "Retrieve task", description = "View task by identifier")
    @ApiResponse(responseCode = "200", description = "View task by identifier")
    @GetMapping("/{taskUuid}")
    fun getTask(@PathVariable taskUuid: UUID): TaskDto {
        return taskService.getTask(taskUuid) ?: throw NotFoundException("Task Not found")
    }

    /**
     * Retrieve Count of completed worker tasks by identifier
     * @param workerUuid the identifier of the worker
     * @return Count of completed worker tasks
     */
    @Operation(summary = "Count of completed worker tasks", description = "Retrieve count of completed worker tasks")
    @ApiResponse(responseCode = "200", description = "Retrieve count of completed worker tasks")
    @GetMapping("/worker/{workerUuid}/completed/count")
    fun getAmountOfCompletedWorkerTasks(@PathVariable workerUuid: UUID): ResponseEntity<Long> {
        return ResponseEntity.ok(taskService.getAmountOfCompletedWorkerTasks(workerUuid))
    }

    /**
     * Retrieve user tasks paginated
     * @return Stored tasks in the system in paginated way sorted.
     * Default sort is date of creation
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER], isOwner = true),
    )
    @Operation(summary = "Retrieve user tasks", description = "View user tasks")
    @ApiResponse(responseCode = "200", description = "Retrieve paginated user tasks")
    @PostMapping("/user/{userUuid}/search")
    fun getUserTasks(@OwnerId @PathVariable userUuid: UUID,
                     @RequestBody taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        return taskService.getUserTasks(userUuid, taskDtoRequest)
    }

    /**
     * Retrieve worker tasks paginated
     * @return Stored tasks in the system in paginated way sorted.
     * Default sort is date of creation
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.WORKER], isOwner = true),
    )
    @Operation(summary = "Retrieve worker tasks", description = "View worker tasks")
    @ApiResponse(responseCode = "200", description = "Retrieve paginated worker tasks")
    @PostMapping("/worker/{workerUuid}/search")
    fun getWorkerTasks(@OwnerId @PathVariable workerUuid: UUID,
                       @RequestBody taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        return taskService.getWorkerTasks(workerUuid, taskDtoRequest)
    }

    /**
     * Retrieve tasks paginated
     * @return Stored tasks in the system in paginated way sorted.
     * Default sort is date of creation
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.ADMIN]),
    )
    @Operation(summary = "Retrieve paginated tasks", description = "View paginated tasks")
    @ApiResponse(responseCode = "200", description = "Retrieve paginated tasks")
    @PostMapping("/search")
    fun getTasks(@Valid @RequestBody taskDtoRequest: TaskDtoRequest): PageOfTaskDto {
        return taskService.getTasks(taskDtoRequest)
    }

    /**
     * Create task without worker assignment
     * @param taskDto the task data to be created
     * @return newly created task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER]),
    )
    @Operation(summary = "Create task", description = "Create task without worker assignment")
    @ApiResponse(responseCode = "200", description = "Newly created task")
    @PostMapping("/")
    fun createTask(@Valid @RequestBody taskDto: TaskDto): TaskDto {
        return taskService.createTask(taskDto)
    }

    /**
     * Update task: change name, description or category.
     * @param taskDto the task data to be updated
     * @return updated task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER], isOwner = true),
    )
    @Operation(summary = "Update task", description = "Update task")
    @ApiResponse(responseCode = "200", description = "Update task")
    @PostMapping("/{userUuid}/{taskUuid}/update")
    fun updateTask(@OwnerId @PathVariable userUuid: UUID,
                   @PathVariable taskUuid: UUID,
                   @Valid @RequestBody taskDto: UpdateTaskDto): TaskDto {
        return taskService.updateTask(taskUuid, taskDto)
    }

    /**
     * Assign a task to a worker
     * @param taskUuid uuid of the task
     * @param workerUUID uuid of the worker
     * @return assigned task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER], isOwner = true),
    )
    @Operation(summary = "Assign task", description = "Assign task to a worker")
    @ApiResponse(responseCode = "200", description = "Assign task")
    @PostMapping("/{userUuid}/{taskUuid}/assign")
    fun assignTask(@OwnerId @PathVariable taskUuid: UUID,
                   @RequestParam workerUUID: UUID): TaskDto {
        return taskService.assignTask(taskUuid, workerUUID)
    }

    /**
     * Unassign a task to from worker. Task should be NEW or ASSIGNED,
     * otherwise it's impossible to unassign a task for a worker
     * @param taskUuid uuid of the task
     * @return Unassigned task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.WORKER], isOwner = true),
    )
    @Operation(summary = "Unassign task", description = "Unassign task from a worker")
    @ApiResponse(responseCode = "200", description = "Unassign task")
    @PostMapping("/{workerUuid}/{taskUuid}/unassign")
    fun unassignTask(@OwnerId @PathVariable workerUuid: UUID,
                     @PathVariable taskUuid: UUID): TaskDto {
        return taskService.unassignTask(taskUuid)
    }


    /**
     * Reassign a task to another worker
     * @param taskUuid uuid of the task
     * @param workerUUID uuid of the new worker
     * @return reassigned task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER], isOwner = true),
    )
    @Operation(summary = "Reassign task", description = "Reassign task to a worker")
    @ApiResponse(responseCode = "200", description = "Reassign task")
    @PostMapping("/{userUuid}/{taskUuid}/reassign")
    fun reassignTask(@OwnerId @PathVariable userUuid: UUID,
                     @PathVariable taskUuid: UUID,
                     @RequestParam workerUUID: UUID): TaskDto {
        return taskService.reassignTask(taskUuid, workerUUID)
    }

    /**
     * Complete task and notify user & worker to leave a review
     * @param taskUuid uuid of the task
     * @return completed task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER, AuthZRole.WORKER], isOwner = true),
    )
    @Operation(summary = "Complete task", description = "Complete task")
    @ApiResponse(responseCode = "200", description = "Complete task")
    @PostMapping("/{userUuid}/{taskUuid}/complete")
    fun completeTask(@OwnerId @PathVariable userUuid: UUID,
                     @PathVariable taskUuid: UUID): TaskDto {
        return taskService.completeTask(taskUuid)
    }

    /**
     * Cancel a task
     * @param taskUuid uuid of the task
     * @return cancel task
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER], isOwner = true),
    )
    @Operation(summary = "Cancel task", description = "Cancel task")
    @ApiResponse(responseCode = "200", description = "Cancel task")
    @PostMapping("/{userUuid}/{taskUuid}/cancel")
    fun cancelTask(@OwnerId @PathVariable userUuid: UUID,
                   @PathVariable taskUuid: UUID): TaskDto {
        return taskService.cancelTask(taskUuid)
    }

    /**
     * Review task by worker
     * @param taskUuid uuid of the task
     * @return Worker Review
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.WORKER], isOwner = true),
    )
    @Operation(summary = "Move task by worker to in progress", description = "Move task by worker to in progress")
    @ApiResponse(responseCode = "200", description = "Move task by worker to in progress")
    @PostMapping("/{taskUuid}/worker/{workerUuid}/confirm")
    fun confirmTaskByWorker(@PathVariable taskUuid: UUID,
                            @OwnerId @PathVariable workerUuid: UUID): TaskDto {
        return taskService.confirmTask(taskUuid, workerUuid)
    }

    /**
     * Review task by worker
     * @param workerReviewDto worker review
     * @return Worker Review
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.WORKER]),
    )
    @Operation(summary = "Review task by worker", description = "Review task by worker")
    @ApiResponse(responseCode = "200", description = "Review task by worker")
    @PostMapping("/worker-review")
    fun reviewTaskByWorker(@Valid @RequestBody workerReviewDto: ReviewDto): ReviewDto {
        return taskService.reviewTaskByWorker(workerReviewDto)
    }

    /**
     * Review task by user
     * @param userReviewDto user review
     * @return User Review
     */
    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER]),
    )
    @Operation(summary = "Review task by user", description = "Review task by user")
    @ApiResponse(responseCode = "200", description = "Review task by user")
    @PostMapping("/user-review")
    fun reviewTaskByUser(@Valid @RequestBody userReviewDto: ReviewDto): ReviewDto {
        return taskService.reviewTaskByUser(userReviewDto)
    }

}