package com.masterello.chat.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    name = "chat",
    indexes = [
        Index(name = "idx_chat_participants", columnList = "user_id, worker_id"),
        Index(name = "idx_chat_task_participants", columnList = "user_id, worker_id, task_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_worker_general_chat",
            columnNames = ["user_id", "worker_id", "chat_type"]
        ),
        UniqueConstraint(
            name = "uk_user_worker_task_chat", 
            columnNames = ["user_id", "worker_id", "task_id"]
        )
    ]
)
data class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "worker_id", nullable = false) 
    val workerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false)
    val chatType: ChatType,

    @Column(name = "task_id")
    val taskId: UUID? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: OffsetDateTime? = null,
) {
    init {
        // Business rule validation
        when (chatType) {
            ChatType.GENERAL -> {
                require(taskId == null) { "General chat cannot have a task ID" }
            }
            ChatType.TASK_SPECIFIC -> {
                require(taskId != null) { "Task-specific chat must have a task ID" }
            }
        }
    }
}

/**
 * Enumeration of chat types to support different conversation contexts.
 */
enum class ChatType {
    /**
     * General chat between a user and worker, not tied to any specific task.
     */
    GENERAL,
    
    /**
     * Chat specific to a particular task between the task owner and assigned worker.
     */
    TASK_SPECIFIC
}
