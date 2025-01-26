package com.masterello.chat.service

import com.masterello.chat.domain.Chat
import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatHistoryDTO
import com.masterello.chat.exceptions.ChatCreationValidationException
import com.masterello.chat.exceptions.ChatNotFoundException
import com.masterello.chat.exceptions.TaskNotFoundException
import com.masterello.chat.exceptions.UserNotFoundException
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageRepository
import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.commons.security.exception.UnauthorisedException
import com.masterello.task.dto.TaskDto
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.user.service.MasterelloUserService
import com.masterello.user.value.MasterelloUser
import com.masterello.worker.service.ReadOnlyWorkerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

@Service
class ChatService(
        private val chatRepository: ChatRepository,
        private val messageRepository: MessageRepository,
        private val messageMapper: MessageMapper,
        private val taskService: ReadOnlyTaskService,
        private val userService: MasterelloUserService,
        private val workerService: ReadOnlyWorkerService) {

    private val log = KotlinLogging.logger {}

    fun getOrCreateChat(workerId: UUID, taskId: UUID): ChatDTO {
        val requester = getRequester()
        val task = getTask(taskId)
        validateWorker(workerId)
        validateChatRequester(requester, workerId, task)
        val users = userService.findAllByIds(setOf(task.userUuid, workerId))
        val user = users[task.userUuid] ?: throw UserNotFoundException("User ${task.userUuid} is not found")
        val worker = users[workerId] ?: throw UserNotFoundException("Worker $workerId is not found")
        val chat = tryToFindExistingChat(workerId, taskId) ?: createChat(workerId, task)

        return ChatDTO(
                id = chat.id!!,
                taskId = chat.taskId!!,
                userId = chat.userId!!,
                userName = getName(user),
                workerId = chat.workerId!!,
                workerName = getName(worker),
        )
    }

    private fun getName(user: MasterelloUser) : String {
        return "${user.name ?: ""} ${user.lastname ?: ""}".trim()
                .ifBlank { user.username }
    }

    fun getChatHistory(chatId: UUID, limit: Int, before: OffsetDateTime): ChatHistoryDTO {
        val chat = chatRepository.findById(chatId)
                .orElseThrow { ChatNotFoundException("Chat $chatId is not found") }
        val requester = getRequester()
        validateChatRequester(requester, chat)
        val messages = fetchMessages(chatId, before, limit)
        return ChatHistoryDTO(messages)
    }

    private fun fetchMessages(chatId: UUID, before: OffsetDateTime, limit: Int) =
            messageRepository.findByChatIdAndCreatedAtBefore(chatId,
                    before,
                    PageRequest.of(0, limit, Sort.by(Sort.Order.desc("createdAt"))))
                    .map(messageMapper::toDto)
                    .reversed()
                    .toList()

    private fun tryToFindExistingChat(workerId: UUID, taskId: UUID): Chat? {
        return chatRepository.findByWorkerIdAndTaskId(workerId, taskId)
    }

    private fun createChat(workerId: UUID, task: TaskDto): Chat {
        return try {
            chatRepository.save(
                    Chat(
                            taskId = task.uuid,
                            workerId = workerId,
                            userId = task.userUuid
                    ))
        } catch (ex: DataIntegrityViolationException) {
            tryToFindExistingChat(workerId, task.uuid!!)
                    ?: throw ChatCreationValidationException("Something went wrong while chat creation")
        }
    }

    private fun validateWorker(workerId: UUID) {
        workerService.getWorkerInfo(workerId)
                .orElseThrow { UserNotFoundException("Worker info $workerId is not found") }
    }

    private fun getTask(taskId: UUID): TaskDto {
        return taskService.getTask(taskId)
                ?: throw TaskNotFoundException("Task $taskId is not found");
    }

    private fun getRequester(): UUID {
        val securityContext = SecurityContextHolder.getContext()
        return (securityContext.authentication as MasterelloAuthentication).details.userId
    }

    private fun validateChatRequester(requesterId: UUID, workerId: UUID, task: TaskDto) {
        if(isTaskOwner(requesterId, task.userUuid)) {
            log.info { "Chat for task ${task.uuid} is requested by task owner: ${task.userUuid}" }
        } else if (isWorker(requesterId, workerId)) {
            log.info { "Chat for task ${task.uuid} is requested by worker: $workerId" }
        } else {
            log.error { "User $requesterId cannot request a chat for task ${task.uuid}"  }
            throw UnauthorisedException("User $requesterId cannot request a chat for task ${task.uuid}")
        }
    }

    private fun validateChatRequester(requesterId: UUID, chat: Chat) {
        if(chat.userId?.let { isTaskOwner(requesterId, it) } == true) {
            log.debug { "Chat ${chat.id} is requested by task owner: $requesterId" }
        } else if (chat.workerId?.let { isWorker(requesterId, it) } == true) {
            log.info { "Chat ${chat.id} is requested by worker: $requesterId" }
        } else {
            log.error { "User $requesterId cannot request a chat ${chat.id}"  }
            throw UnauthorisedException("User $requesterId cannot request a chat ${chat.id}")
        }
    }

    private fun isWorker(requesterId: UUID, workerId: UUID): Boolean {
        return requesterId == workerId
    }

    private fun isTaskOwner(requesterId: UUID, taskOwnerId: UUID): Boolean {
        return requesterId == taskOwnerId
    }
}