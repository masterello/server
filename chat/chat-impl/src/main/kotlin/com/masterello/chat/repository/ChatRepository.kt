package com.masterello.chat.repository

import com.masterello.chat.domain.Chat
import com.masterello.chat.domain.ChatType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatRepository : JpaRepository<Chat, UUID> {
    
    /**
     * Finds a general chat between a user and worker.
     */
    fun findByUserIdAndWorkerIdAndChatType(userId: UUID, workerId: UUID, chatType: ChatType): Chat?
    
    /**
     * Finds a task-specific chat between user and worker for a specific task.
     */
    fun findByUserIdAndWorkerIdAndTaskIdAndChatType(
        userId: UUID, 
        workerId: UUID, 
        taskId: UUID, 
        chatType: ChatType
    ): Chat?
    
    
    /**
     * Finds all chats for a specific user (where they are either user or worker).
     */
    @Query("""
        SELECT c FROM Chat c 
        WHERE (c.userId = :userId OR c.workerId = :userId) 
        ORDER BY c.createdAt DESC
    """)
    fun findAllChatsForUser(@Param("userId") userId: UUID): List<Chat>
}
