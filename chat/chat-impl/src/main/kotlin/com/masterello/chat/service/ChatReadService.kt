package com.masterello.chat.service

import com.masterello.chat.dto.MarkReadRequest
import com.masterello.chat.dto.ReadReceiptDTO
import com.masterello.chat.repository.MessageReadRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
class ChatReadService(
    private val messageReadRepository: MessageReadRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    @Transactional
    fun markRead(chatId: UUID, readerId: UUID, request: MarkReadRequest): Int {
        val now = OffsetDateTime.now()
        val updated: Int = if (!request.messageIds.isNullOrEmpty()) {
            messageReadRepository.markReadByIds(readerId, request.messageIds!!, now)
        } else {
            val cutoff = request.visibleUpTo ?: now
            messageReadRepository.markReadUpToTime(chatId, readerId, cutoff, now)
        }
        if (updated > 0) {
            // Broadcast read receipt to chat topic for realtime UI updates
            messagingTemplate.convertAndSend(
                "/topic/read-receipts/$chatId",
                ReadReceiptDTO(
                    chatId = chatId,
                    readerId = readerId,
                    readAt = now,
                    messageIds = request.messageIds,
                    visibleUpTo = request.visibleUpTo ?: now
                )
            )
        }
        return updated
    }
}