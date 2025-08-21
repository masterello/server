package com.masterello.chat.ws

import com.masterello.chat.dto.TypingEvent
import com.masterello.chat.util.AuthUtil.getUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class TypingController {
    private val log = KotlinLogging.logger {}

    @MessageMapping("/typing/{chatId}")
    @SendTo("/topic/typing/{chatId}")
    fun typing(
        incoming: TypingEvent,
        @DestinationVariable chatId: UUID,
        accessor: StompHeaderAccessor
    ): TypingEvent {
        val user = getUser(accessor) ?: throw IllegalStateException("No authentication data found in session")
        val started = incoming.started
        log.trace { "Typing event user=${user.userId} chat=$chatId started=$started" }
        return TypingEvent(userId = user.userId, started = started)
    }
}
