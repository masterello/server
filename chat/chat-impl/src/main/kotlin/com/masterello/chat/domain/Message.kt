package com.masterello.chat.domain;

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.*


@Entity
@Table(name = "chat_message")
data class Message(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: UUID? = null,

        @Column(name = "chat_id")
        val chatId: UUID? = null,

        @Column(name = "message")
        val message: String? = null,

        @Column(name = "created_by")
        val createdBy: UUID? = null,

        @Column(name = "created_at")
        @CreationTimestamp
        val createdAt: OffsetDateTime? = null
)