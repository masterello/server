package com.masterello.chat.repository

import com.masterello.chat.domain.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {

    fun findByChatIdAndCreatedAtBefore(chatId : UUID, before: OffsetDateTime, page: PageRequest) : Page<Message>

    fun findTopByChatIdOrderByCreatedAtDesc(chatId: UUID): Message?
}
