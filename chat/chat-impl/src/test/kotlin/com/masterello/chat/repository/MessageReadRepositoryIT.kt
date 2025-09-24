package com.masterello.chat.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import com.masterello.chat.ChatTestConfiguration
import com.masterello.commons.test.AbstractDBIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.time.OffsetDateTime
import java.util.UUID

@SqlGroup(
    Sql(scripts = ["classpath:sql/create-chat-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["classpath:sql/clean.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
)
@SpringBootTest(classes = [ChatTestConfiguration::class])
class MessageReadRepositoryIT : AbstractDBIntegrationTest() {

    @Autowired
    private lateinit var repo: MessageReadRepository

    private val chatId = UUID.fromString("e5fcf8dd-b6be-4a36-a85a-e2d952cc6254")
    private val userId = UUID.fromString("bb2c6e16-2228-4ac1-8482-1f3548672b43")
    private val workerId = UUID.fromString("57bc029c-d8e3-458f-b25a-7f73283cec98")

    @Test
    fun unreadPerChat_returnsCountsForRecipient() {
        val listForUser = repo.unreadPerChat(userId)
        val countForChatUser = listForUser.first { it.getChatId() == chatId }.getUnread()
        assertEquals(2L, countForChatUser)

        val listForWorker = repo.unreadPerChat(workerId)
        val countForChatWorker = listForWorker.first { it.getChatId() == chatId }.getUnread()
        assertEquals(2L, countForChatWorker)
    }

    @Test
    fun markReadByIds_marksOnlyProvided() {
        val m2 = UUID.fromString("2b3c4d5e-5555-6666-7777-888888888888")
        // Initially 2 unread for user
        assertEquals(2L, repo.countByIdRecipientIdAndReadAtIsNull(userId))
        // Mark only m2
        val updated = repo.markReadByIds(userId, listOf(m2), OffsetDateTime.parse("2025-01-12T10:40:00Z"))
        assertEquals(1, updated)
        // Now unread should be 1 for user
        assertEquals(1L, repo.countByIdRecipientIdAndReadAtIsNull(userId))
    }

    @Test
    fun markReadUpToTime_marksByCreatedAtThreshold() {
        // Initially 2 unread for user (m2 10:16, m4 10:18)
        assertEquals(2L, repo.countByIdRecipientIdAndReadAtIsNull(userId))
        // Mark up to 10:17:00 -> should mark only m2
        val updated1 = repo.markReadUpToTime(chatId, userId, OffsetDateTime.parse("2025-01-12T10:17:00Z"), OffsetDateTime.parse("2025-01-12T10:50:00Z"))
        assertEquals(1, updated1)
        assertEquals(1L, repo.countByIdRecipientIdAndReadAtIsNull(userId))
        // Mark up to 10:18:00 -> should mark m4 now
        val updated2 = repo.markReadUpToTime(chatId, userId, OffsetDateTime.parse("2025-01-12T10:18:00Z"), OffsetDateTime.parse("2025-01-12T10:51:00Z"))
        assertEquals(1, updated2)
        assertEquals(0L, repo.countByIdRecipientIdAndReadAtIsNull(userId))
    }

    @Test
    fun countByIdRecipientIdAndReadAtIsNull_countsForBothRecipients() {
        // Initial fixture: two unread for user (m2, m4) and two unread for worker (m1, m3)
        assertEquals(2L, repo.countByIdRecipientIdAndReadAtIsNull(userId))
        assertEquals(2L, repo.countByIdRecipientIdAndReadAtIsNull(workerId))
    }
}
