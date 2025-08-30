package com.masterello.chat.service

import com.masterello.chat.domain.Chat
import com.masterello.chat.domain.ChatType
import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatHistoryDTO
import com.masterello.chat.exceptions.ChatCreationValidationException
import com.masterello.chat.exceptions.TaskNotFoundException
import com.masterello.chat.exceptions.WorkerNotFoundException
import com.masterello.chat.mapper.ChatMapper
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageRepository
import com.masterello.commons.security.util.AuthContextUtil.getAuthenticatedUserId
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.user.service.MasterelloUserService
import com.masterello.user.value.MasterelloUser
import com.masterello.worker.service.ReadOnlyWorkerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val messageMapper: MessageMapper,
    private val userService: MasterelloUserService,
    private val chatMapper: ChatMapper,
    private val taskService: ReadOnlyTaskService,
    private val workerService: ReadOnlyWorkerService
) {

    private val log = KotlinLogging.logger {}

    /**
     * Creates or retrieves a general chat between a user and worker.
     * Authorization is handled at the controller level with @PreAuthorize.
     */
    fun getOrCreateGeneralChat(userId: UUID, workerId: UUID): ChatDTO {
        log.info { "Creating/retrieving general chat: user=$userId, worker=$workerId" }

        // Worker must exist
        validateWorker(workerId)
        // Try to find existing general chat
        val chat = findExistingGeneralChat(userId, workerId)
            ?: createGeneralChat(userId, workerId)
        
        // Get participant information and return DTO
        val participantsInfo = getChatParticipantsInfo(userId, workerId)
        return chatMapper.toDTO(chat, participantsInfo)
    }

    private fun validateWorker(workerId: UUID) {
        workerService.getWorkerInfo(workerId).orElseThrow { WorkerNotFoundException("Worker with id $workerId not found") }
    }

    /**
     * Creates or retrieves a task-specific chat between task owner and worker.
     * Authorization is handled at the controller level with @PreAuthorize.
     */
    fun getOrCreateTaskChat(taskId: UUID, workerId: UUID): ChatDTO {
        val requesterId = getAuthenticatedUserId()
        
        log.info { "Creating/retrieving task chat: requester=$requesterId, task=$taskId, worker=$workerId" }

        validateWorker(workerId)
        // Get task information to determine the actual task owner and assigned worker
        val task = taskService.getTask(taskId)
            ?: throw TaskNotFoundException("Task $taskId not found")
        
        // The chat is always between task owner (userUuid) and assigned worker (workerUuid)
        val taskOwnerId = task.userUuid

        // Try to find existing task chat between task owner and assigned worker
        val chat = findExistingTaskChat(taskOwnerId, workerId, taskId)
            ?: createTaskChat(taskOwnerId, workerId, taskId)
        
        // Get participant information and return DTO
        val participantsInfo = getChatParticipantsInfo(taskOwnerId, workerId)
        return chatMapper.toDTO(chat, participantsInfo)
    }

    /**
     * Retrieves chat history.
     * Authorization is handled at the controller level with @PreAuthorize.
     */
    fun getChatHistory(chatId: UUID, limit: Int, before: OffsetDateTime): ChatHistoryDTO {
        log.info { "Retrieving chat history: chatId=$chatId, limit=$limit" }
        
        // Fetch and return messages
        val messages = fetchMessages(chatId, before, limit)
        return ChatHistoryDTO(messages)
    }
    
    /**
     * Gets all active chats for the current user.
     */
    fun getUserChats(page: Int, size: Int): com.masterello.chat.dto.ChatPageDTO {
        val me = getAuthenticatedUserId()
        val pageable = org.springframework.data.domain.PageRequest.of(
            (page.coerceAtLeast(1) - 1), size,
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("lastMessageAt"),
                org.springframework.data.domain.Sort.Order.desc("id")
            )
        )
        val p = chatRepository.findByUserIdOrWorkerId(me, me, pageable)
        val participants = p.content.flatMap { listOf(it.userId, it.workerId) }.toSet()
        val info = userService.findAllByIds(participants)
        val items = p.content.map { chatMapper.toDTO(it, info) }
        return com.masterello.chat.dto.ChatPageDTO(
            items = items,
            page = page.coerceAtLeast(1),
            size = size,
            totalPages = p.totalPages,
            totalElements = p.totalElements,
            hasNext = p.hasNext(),
            hasPrevious = p.hasPrevious()
        )
    }

    private fun findExistingGeneralChat(userId: UUID, workerId: UUID): Chat? {
        return chatRepository.findByUserIdAndWorkerIdAndChatType(userId, workerId, ChatType.GENERAL)
    }

    private fun findExistingTaskChat(userId: UUID, workerId: UUID, taskId: UUID): Chat? {
        return chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(
            userId, workerId, taskId, ChatType.TASK_SPECIFIC
        )
    }

    private fun createGeneralChat(userId: UUID, workerId: UUID): Chat {
        return try {
            val now = OffsetDateTime.now()
            val chat = Chat(
                userId = userId,
                workerId = workerId,
                chatType = ChatType.GENERAL,
                taskId = null,
                lastMessageAt = now
            )
            chatRepository.save(chat)
        } catch (ex: DataIntegrityViolationException) {
            // Handle race condition - try to find the chat that was created by another thread
            findExistingGeneralChat(userId, workerId)
                ?: throw ChatCreationValidationException("Failed to create general chat between $userId and $workerId")
        }
    }


    private fun createTaskChat(userId: UUID, workerId: UUID, taskId: UUID): Chat {
        return try {
            val now = OffsetDateTime.now()
            val chat = Chat(
                userId = userId,
                workerId = workerId,
                chatType = ChatType.TASK_SPECIFIC,
                taskId = taskId,
                lastMessageAt = now
            )
            chatRepository.save(chat)
        } catch (ex: DataIntegrityViolationException) {
            // Handle race condition - try to find the chat that was created by another thread
            findExistingTaskChat(userId, workerId, taskId)
                ?: throw ChatCreationValidationException("Failed to create task chat for task $taskId")
        }
    }

    private fun getChatParticipantsInfo(userId: UUID, workerId: UUID): Map<UUID, MasterelloUser> {
        return userService.findAllByIds(setOf(userId, workerId))
    }

    private fun fetchMessages(chatId: UUID, before: OffsetDateTime, limit: Int) =
        messageRepository.findByChatIdAndCreatedAtBefore(
            chatId,
            before,
            PageRequest.of(0, limit, Sort.by(Sort.Order.desc("createdAt")))
        )
            .map(messageMapper::toDto)
            .reversed()
            .toList()

}
