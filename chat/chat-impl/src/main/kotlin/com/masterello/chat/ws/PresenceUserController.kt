package com.masterello.chat.ws

import com.masterello.chat.dto.PresenceStatus
import com.masterello.chat.dto.UserPresenceDTO
import com.masterello.chat.ws.session.SessionRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class PresenceUserController(
    private val sessionRegistry: SessionRegistry
) {
    private val log = KotlinLogging.logger {}

    // One-time snapshot sent to the subscriber upon subscribing to /app/presence/user/{userId}
    @SubscribeMapping("/presence/user/{userId}")
    fun presenceSnapshot(@DestinationVariable userId: UUID): UserPresenceDTO {
        val online = sessionRegistry.anyOnline(userId)
        val dto = UserPresenceDTO(
            userId = userId,
            status = if (online) PresenceStatus.ONLINE else PresenceStatus.OFFLINE
        )
        log.debug { "Presence snapshot user=$userId -> $dto" }
        return dto
    }
}
