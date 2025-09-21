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

    fun findByUserIdOrWorkerId(userId: UUID, workerId: UUID, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<Chat>

    @Query(
        "SELECT c FROM Chat c WHERE (c.userId = :me OR c.workerId = :me) AND c.lastMessageAt IS NOT NULL"
    )
    fun findNonEmptyChatsFor(
        @Param("me") me: UUID,
        pageable: org.springframework.data.domain.Pageable
    ): org.springframework.data.domain.Page<Chat>

    /**
     * Cursor-based fetch of non-empty chats for infinite scroll, ordered by lastMessageAt DESC, id DESC.
     * First page (no cursor):
     */
    @Query(
        "SELECT c FROM Chat c " +
        "WHERE (c.userId = :me OR c.workerId = :me) " +
        "AND c.lastMessageAt IS NOT NULL"
    )
    fun findNonEmptyChatsForScrollFirstPage(
        @Param("me") me: UUID,
        pageable: org.springframework.data.domain.Pageable
    ): List<Chat>

    /**
     * Subsequent pages (with cursor before lastMessageAt):
     */
    @Query(
        "SELECT c FROM Chat c " +
        "WHERE (c.userId = :me OR c.workerId = :me) " +
        "AND c.lastMessageAt IS NOT NULL " +
        "AND c.lastMessageAt < :cursor"
    )
    fun findNonEmptyChatsForScrollAfter(
        @Param("me") me: UUID,
        @Param("cursor") cursor: java.time.OffsetDateTime,
        pageable: org.springframework.data.domain.Pageable
    ): List<Chat>

    /**
     * Atomically update denormalized last message fields. Updates preview only if this timestamp is the newest.
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query(
        "UPDATE Chat c SET c.lastMessageAt = :createdAt, c.lastMessageBy = :createdBy, c.lastMessagePreview = :preview " +
        "WHERE c.id = :chatId AND (c.lastMessageAt IS NULL OR :createdAt >= c.lastMessageAt)"
    )
    fun updateLastMessage(
        @Param("chatId") chatId: UUID,
        @Param("createdAt") createdAt: java.time.OffsetDateTime,
        @Param("createdBy") createdBy: UUID,
        @Param("preview") preview: String
    ): Int
}
