package com.masterello.chat.ws

import com.masterello.chat.dto.PresenceEntry
import com.masterello.chat.dto.PresenceSnapshotDTO
import com.masterello.chat.dto.PresenceStatus
import com.masterello.chat.presence.PresenceService
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.util.AuthUtil.getUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class PresenceController(
    private val chatRepository: ChatRepository,
    private val presenceService: PresenceService
) {
    private val log = KotlinLogging.logger {}

    @MessageMapping("/presence/ping/{chatId}")
    @SendTo("/topic/presence/{chatId}")
    fun pingPresence(@DestinationVariable chatId: UUID, accessor: StompHeaderAccessor): PresenceSnapshotDTO {
        val sessionId = accessor.sessionId
        val user = getUser(accessor)
        if (sessionId != null && user != null) {
            presenceService.touch(user.userId, sessionId)
        }

        // Determine chat participants
        val chat = chatRepository.findById(chatId).orElse(null)
            ?: return PresenceSnapshotDTO(chatId, emptyList())

        val userIds = listOf(chat.userId, chat.workerId)
        val entries = userIds.map { uid ->
            val online = presenceService.isOnline(uid)
            PresenceEntry(
                userId = uid,
                status = if (online) PresenceStatus.ONLINE else PresenceStatus.OFFLINE,
                lastSeen = presenceService.getLastSeen(uid)
            )
        }
        log.debug { "Presence snapshot for chat=$chatId -> $entries" }
        return PresenceSnapshotDTO(chatId, entries)
    }
}
