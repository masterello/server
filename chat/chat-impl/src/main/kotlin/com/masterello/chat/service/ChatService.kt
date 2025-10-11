package com.masterello.chat.service

import com.masterello.chat.domain.Chat
import com.masterello.chat.domain.ChatType
import com.masterello.chat.domain.Message
import com.masterello.chat.domain.MessageRead
import com.masterello.chat.dto.*
import com.masterello.chat.exceptions.ChatAlreadyExistsException
import com.masterello.chat.exceptions.ChatNotFoundException
import com.masterello.chat.exceptions.TaskNotFoundException
import com.masterello.chat.exceptions.WorkerNotFoundException
import com.masterello.chat.mapper.ChatMapper
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageReadRepository
import com.masterello.chat.repository.MessageRepository
import com.masterello.commons.security.util.AuthContextUtil.getAuthenticatedUserId
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.user.service.MasterelloUserService
import com.masterello.user.value.MasterelloUser
import com.masterello.worker.service.ReadOnlyWorkerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
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
        private val workerService: ReadOnlyWorkerService,
        private val messageReadRepository: MessageReadRepository,
) {

    private val log = KotlinLogging.logger {}

    /**
     * Creates or retrieves a general chat between a user and worker.
     * Authorization is handled at the controller level with @PreAuthorize.
     */
    fun getGeneralChat(userId: UUID, workerId: UUID): ChatDTO {
        log.info { "Get general chat: user=$userId, worker=$workerId" }
        val chat = findExistingGeneralChat(userId, workerId)
                ?: throw ChatNotFoundException("General chat between $userId and $workerId not found")
        val me = getAuthenticatedUserId()
        val unread = messageReadRepository.countByIdRecipientIdAndChatIdAndReadAtIsNull(me, chat.id!!)
        val participantsInfo = getChatParticipantsInfo(userId, workerId)
        return chatMapper.toDTO(chat, participantsInfo, unread)
    }

    fun createGeneralChatPublic(userId: UUID, workerId: UUID): ChatDTO {
        log.info { "Create general chat: user=$userId, worker=$workerId" }
        validateWorker(workerId)
        val existing = findExistingGeneralChat(userId, workerId)
        if (existing != null) throw ChatAlreadyExistsException("General chat between $userId and $workerId already exists")
        val chat = createGeneralChat(userId, workerId)
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
    fun getTaskChat(taskId: UUID, workerId: UUID): ChatDTO {
        val requesterId = getAuthenticatedUserId()
        log.info { "Get task chat: requester=$requesterId, task=$taskId, worker=$workerId" }
        val task = taskService.getTask(taskId)
                ?: throw TaskNotFoundException("Task $taskId not found")
        val taskOwnerId = task.userUuid
        val chat = findExistingTaskChat(taskOwnerId, workerId, taskId)
                ?: throw ChatNotFoundException("Task chat for task $taskId and worker $workerId not found")
        val participantsInfo = getChatParticipantsInfo(taskOwnerId, workerId)
        return chatMapper.toDTO(chat, participantsInfo)
    }

    fun createTaskChatPublic(taskId: UUID, workerId: UUID): ChatDTO {
        val requesterId = getAuthenticatedUserId()
        log.info { "Create task chat: requester=$requesterId, task=$taskId, worker=$workerId" }
        validateWorker(workerId)
        val task = taskService.getTask(taskId) ?: throw TaskNotFoundException("Task $taskId not found")
        val taskOwnerId = task.userUuid
        val existing = findExistingTaskChat(taskOwnerId, workerId, taskId)
        if (existing != null) throw ChatAlreadyExistsException("Task chat for task $taskId and worker $workerId already exists")
        val chat = createTaskChat(taskOwnerId, workerId, taskId)
        val participantsInfo = getChatParticipantsInfo(taskOwnerId, workerId)
        return chatMapper.toDTO(chat, participantsInfo)
    }

    /**
     * Retrieves chat history.
     * Authorization is handled at the controller level with @PreAuthorize.
     */
    fun getChatHistory(chatId: UUID, limit: Int, before: OffsetDateTime): ChatHistoryDTO {
        log.info { "Retrieving chat history: chatId=$chatId, limit=$limit" }

        // Fetch limit+1 messages to determine if there are more
        log.debug { "Fetch messages for chat history: chatId=$chatId" }
        val messagesList = fetchMessages(chatId, before, limit + 1)
        log.debug { "Fetched messages for chat history: chatId=$chatId: $messagesList.size" }
        val hasMore = messagesList.size > limit
        
        // Take only the requested limit
        val messagesToReturn = if (hasMore) messagesList.dropLast(1) else messagesList
        log.debug { "Fetch message reads for chat history: chatId=$chatId" }
        val reads = fetchReads(messagesToReturn)
        log.debug { "Fetched message reads for chat history: chatId=$chatId" }

        val messages = messagesToReturn
                .map { m -> messageMapper.toDto(m, reads.getOrDefault(m.id, emptyList())) }
                .reversed()
        log.debug { "Meessages mapped to DTO for chat history: chatId=$chatId" }

        val nextCursor = messages.firstOrNull()?.createdAt
        log.info { "Retrieved chat history: chatId=$chatId, limit=$limit" }
        return ChatHistoryDTO(
                messages = messages,
                nextCursor = nextCursor,
                hasMore = hasMore
        )
    }

    fun fetchMessages(chatId: UUID, before: OffsetDateTime, limit: Int): List<Message> {
        val pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending())
        return messageRepository.findByChatIdAndCreatedAtBefore(chatId, before, pageable)
    }

    fun fetchReads(messages: Iterable<Message>): Map<UUID, List<MessageRead>> {
        val ids = messages.mapNotNull { it.id }
        return if (ids.isNotEmpty()) {
            messageReadRepository.findAllByMessageIds(ids).groupBy { it.id.messageId }
        } else emptyMap()
    }

    /**
     * Gets all active chats for the current user.
     */
    fun getUserChats(page: Int, size: Int): ChatPageDTO {
        val me = getAuthenticatedUserId()
        val pageable = PageRequest.of(
                (page.coerceAtLeast(1) - 1), size,
                Sort.by(
                        Sort.Order.desc("lastMessageAt"),
                        Sort.Order.desc("id")
                )
        )
        val p = chatRepository.findNonEmptyChatsFor(me, pageable)
        val participants = p.content.flatMap { listOf(it.userId, it.workerId) }.toSet()
        val info = userService.findAllByIds(participants)
        val unreadMap: Map<UUID, Long> = messageReadRepository.unreadPerChat(me)
                .associate { it.getChatId() to it.getUnread() }
        val items = p.content.map { c ->
            chatMapper.toDTO(c, info, unreadMap[c.id!!] ?: 0L)
        }
        return ChatPageDTO(
                items = items,
                page = page.coerceAtLeast(1),
                size = size,
                totalPages = p.totalPages,
                totalElements = p.totalElements,
                hasNext = p.hasNext(),
                hasPrevious = p.hasPrevious()
        )
    }

    /**
     * Cursor-based fetch of user's chats for infinite scroll.
     */
    fun getUserChatsScroll(cursor: OffsetDateTime?, limit: Int): ChatScrollDTO {
        val me = getAuthenticatedUserId()
        val pageable = PageRequest.of(
                0,
                limit,
                Sort.by(
                        Sort.Order.desc("lastMessageAt"),
                        Sort.Order.desc("id")
                )
        )
        val list = if (cursor == null) {
            chatRepository.findNonEmptyChatsForScrollFirstPage(me, pageable)
        } else {
            chatRepository.findNonEmptyChatsForScrollAfter(me, cursor, pageable)
        }
        val participants = list.flatMap { listOf(it.userId, it.workerId) }.toSet()
        val info = userService.findAllByIds(participants)
        val unreadMap: Map<UUID, Long> = messageReadRepository.unreadPerChat(me).associate { it.getChatId() to it.getUnread() }
        val items = list.map { c ->
            chatMapper.toDTO(c, info, unreadMap[c.id!!] ?: 0L)
        }
        val nextCursor = list.lastOrNull()?.lastMessageAt
        val hasMore = list.size >= limit
        return ChatScrollDTO(
                items = items,
                nextCursor = nextCursor,
                hasMore = hasMore
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
        return createChatInternal(
                userId = userId,
                workerId = workerId,
                chatType = ChatType.GENERAL,
                taskId = null
        )
    }

    private fun createTaskChat(userId: UUID, workerId: UUID, taskId: UUID): Chat {
        return createChatInternal(
                userId = userId,
                workerId = workerId,
                chatType = ChatType.TASK_SPECIFIC,
                taskId = taskId
        )
    }

    private fun createChatInternal(
            userId: UUID,
            workerId: UUID,
            chatType: ChatType,
            taskId: UUID?
    ): Chat {
        return try {
            val chat = Chat(
                    userId = userId,
                    workerId = workerId,
                    chatType = chatType,
                    taskId = taskId,
            )
            chatRepository.save(chat)
        } catch (ex: DataIntegrityViolationException) {
            // Unique constraint triggered -> chat already exists
            throw ChatAlreadyExistsException("Chat already exists for participants")
        }
    }

    private fun getChatParticipantsInfo(userId: UUID, workerId: UUID): Map<UUID, MasterelloUser> {
        return userService.findAllByIds(setOf(userId, workerId))
    }
}
