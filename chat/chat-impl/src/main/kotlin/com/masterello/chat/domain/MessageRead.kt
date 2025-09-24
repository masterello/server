package com.masterello.chat.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "message_reads")
data class MessageRead(
    @EmbeddedId
    val id: MessageReadId,

    @Column(name = "chat_id", nullable = false)
    val chatId: UUID,

    @Column(name = "read_at")
    var readAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Embeddable
data class MessageReadId(
    @Column(name = "message_id")
    val messageId: UUID,

    @Column(name = "recipient_id")
    val recipientId: UUID
)