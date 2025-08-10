package com.masterello.chat.security

import com.masterello.chat.repository.ChatRepository
import com.masterello.commons.security.util.AuthContextUtil.getAuthenticatedUserId
import com.masterello.task.service.ReadOnlyTaskService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

/**
 * Contains authorization expressions for use with @PreAuthorize annotations.
 * This service provides reusable security expressions for chat operations.
 */
@Component("chatSecurity")
class ChatSecurityExpressions(
    private val chatRepository: ChatRepository,
    private val taskService: ReadOnlyTaskService,
) {

    private val log = KotlinLogging.logger {}

    /**
     * Checks if the current user can create a general chat with the specified worker.
     * Uses SecurityContextHolder for REST endpoints.
     */
    fun canCreateGeneralChat(userId: UUID, workerId: UUID): Boolean {
        val currentUserId = getAuthenticatedUserId()
        return canCreateGeneralChat(currentUserId, userId, workerId)
    }
    
    /**
     * Checks if the specified user can create a general chat with the specified worker.
     * This version accepts userId parameter for WebSocket contexts.
     */
    fun canCreateGeneralChat(currentUserId: UUID, userId: UUID, workerId: UUID): Boolean {
        // Cannot create chat with yourself
        if (userId == workerId) {
            return false
        }

        when (currentUserId) {
            userId -> {
                log.debug { "Chat is requested by user: $userId" }
            }
            workerId -> {
                log.debug { "Chat is requested by worker: $workerId" }
            }
            else -> {
                log.error { "User $currentUserId cannot request a chat between user: $userId and $workerId"  }
                return false
            }
        }
        
        // Worker must exist
        return true
    }

    /**
     * Checks if the current user can create or access a task-specific chat.
     */
    fun canCreateTaskChat(taskId: UUID, workerId: UUID): Boolean {
        val currentUserId = getAuthenticatedUserId()
        
        // Task must exist
        val task = taskService.getTask(taskId) ?: return false
        
        // User must be either task owner or worker
        return task.userUuid == currentUserId ||  currentUserId == workerId
    }

    /**
     * Checks if the current user can access the specified chat.
     */
    fun canAccessChat(chatId: UUID): Boolean {
        val currentUserId = getAuthenticatedUserId()
        return canAccessChat(currentUserId, chatId)
    }
    
    /**
     * Checks if the specified user can access the specified chat.
     * This version accepts userId parameter for WebSocket contexts.
     */
    fun canAccessChat(userId: UUID, chatId: UUID): Boolean {
        val chat = chatRepository.findById(chatId).orElse(null) ?: return false
        return chat.userId == userId || chat.workerId == userId
    }

    /**
     * Checks if the current user can send messages to the specified chat.
     */
    fun canSendMessageToChat(chatId: UUID): Boolean {
        val currentUserId = getAuthenticatedUserId()
        return canSendMessageToChat(currentUserId, chatId)
    }
    
    /**
     * Checks if the specified user can send messages to the specified chat.
     * This version accepts userId parameter for WebSocket contexts.
     */
    fun canSendMessageToChat(userId: UUID, chatId: UUID): Boolean {
        val chat = chatRepository.findById(chatId).orElse(null) ?: return false
        return chat.userId == userId || chat.workerId == userId
    }
    
    /**
     * Checks if the specified user can subscribe to WebSocket updates for the specified chat.
     * This version accepts userId parameter for WebSocket contexts.
     */
    fun canSubscribeToChat(userId: UUID, chatId: UUID): Boolean {
        return canAccessChat(userId, chatId)
    }
}
