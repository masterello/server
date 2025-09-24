package com.masterello.chat.repository

import com.masterello.chat.domain.MessageRead
import com.masterello.chat.domain.MessageReadId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

interface MessageReadRepository : JpaRepository<MessageRead, MessageReadId> {

    fun countByIdRecipientIdAndReadAtIsNull(recipientId: UUID): Long

    interface UnreadPerChat {
        fun getChatId(): UUID
        fun getUnread(): Long
    }

    @Query(
        "select r.chatId as chatId, count(r) as unread " +
        "from MessageRead r " +
        "where r.id.recipientId = :recipientId and r.readAt is null " +
        "group by r.chatId"
    )
    fun unreadPerChat(@Param("recipientId") recipientId: UUID): List<UnreadPerChat>

    @Modifying
    @Transactional
    @Query(
        "update MessageRead r set r.readAt = :readAt " +
        "where r.id.recipientId = :recipientId and r.readAt is null and r.id.messageId in :messageIds"
    )
    fun markReadByIds(
        @Param("recipientId") recipientId: UUID,
        @Param("messageIds") messageIds: List<UUID>,
        @Param("readAt") readAt: OffsetDateTime
    ): Int

    @Modifying
    @Transactional
    @Query(
        "update MessageRead r set r.readAt = :readAt " +
        "where r.id.recipientId = :recipientId " +
        "and r.chatId = :chatId " +
        "and r.readAt is null " +
        "and r.createdAt <= :visibleUpto"
    )
    fun markReadUpToTime(
        @Param("chatId") chatId: UUID,
        @Param("recipientId") recipientId: UUID,
        @Param("visibleUpto") visibleUpto: OffsetDateTime,
        @Param("readAt") readAt: OffsetDateTime
    ): Int
}
