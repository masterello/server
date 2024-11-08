package com.masterello.chat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.auth.data.AuthData
import com.masterello.auth.data.AuthZRole
import com.masterello.chat.ChatTestConfiguration
import com.masterello.chat.ChatTestDataProvider.Companion.CHAT
import com.masterello.chat.ChatTestDataProvider.Companion.USER
import com.masterello.chat.ChatTestDataProvider.Companion.WORKER_WITH_CHAT
import com.masterello.chat.ChatTestDataProvider.Companion.tokenCookie
import com.masterello.chat.dto.ChatMessageDTO
import com.masterello.commons.test.AbstractWebIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@SqlGroup(Sql(scripts = ["classpath:sql/create-chat-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["classpath:sql/clean.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ChatTestConfiguration::class])
class ChatWebSocketIntegrationTest : AbstractWebIntegrationTest() {


    private lateinit var stompClient: WebSocketStompClient
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        stompClient = WebSocketStompClient(StandardWebSocketClient())
        val messageConverter = MappingJackson2MessageConverter()
        messageConverter.objectMapper = objectMapper
        stompClient.messageConverter = messageConverter
    }

    @Test
    fun `test new messages received`() {
        val userSession = initTheSessionForUser(USER, listOf(AuthZRole.USER))
        val userQueue = subscribe(userSession)


        val workerSession = initTheSessionForUser(WORKER_WITH_CHAT, listOf(AuthZRole.WORKER))
        val workerQueue  = subscribe(workerSession)

        userSession.send("/ws/sendMessage/$CHAT", "Message to worker")

        val messageW1 = workerQueue.poll(1, TimeUnit.SECONDS)
        assertEquals(messageW1.message, "Message to worker")
        val messageU1 = userQueue.poll(1, TimeUnit.SECONDS)
        assertEquals(messageU1.message, "Message to worker")

        workerSession.send("/ws/sendMessage/$CHAT", "Message to user")
        val messageW2 = workerQueue.poll(1, TimeUnit.SECONDS)
        assertEquals(messageW2.message, "Message to user")
        val messageU2 = userQueue.poll(1, TimeUnit.SECONDS)
        assertEquals(messageU2.message, "Message to user")
    }

    private fun initTheSessionForUser(userId: UUID, roles: List<AuthZRole>): StompSession {
        authenticate(userId, roles)
        return initTheSession()
    }
    private fun initTheSession(): StompSession {
        val tokenCookie = tokenCookie()
        val headers = HttpHeaders()
        headers.add("Cookie", "${tokenCookie.name}=${tokenCookie.value}")

        return stompClient
                .connectAsync("ws://localhost:$port/ws/chat/websocket", WebSocketHttpHeaders(headers), TestStompSessionHandler())
                .get(1, TimeUnit.SECONDS)
    }

    private fun subscribe(session: StompSession): BlockingQueue<ChatMessageDTO> {
        val queue = LinkedBlockingQueue<ChatMessageDTO>();
        session.subscribe("/topic/messages/$CHAT", TestStompFrameHandler(queue))
        return queue;
    }

    private fun authenticate(userId: UUID, roles: List<AuthZRole>) {
        // Set up the mock for validateToken using the values from the annotation
        whenever<Optional<AuthData>>(authService.validateToken(Mockito.anyString()))
                .thenReturn(Optional.of(AuthData.builder()
                        .userId(userId)
                        .userRoles(roles)
                        .emailVerified(true)
                        .build()))
    }
    private fun getExpectedHistory(): List<ChatMessageDTO> {
        return listOf(
                ChatMessageDTO(
                        id = UUID.fromString("1f4e6b79-bc8e-4b7e-9f1a-5ad9e85645f2"),
                        chatId = CHAT,
                        message = "Hello, World!",
                        createdBy = USER,
                        createdAt = OffsetDateTime.parse("2025-01-12T10:15:30+00:00")
                ),
                ChatMessageDTO(
                        id = UUID.fromString("2a6f4c8d-d0e6-476d-a0d1-3b9b6c5c78e9"),
                        chatId = CHAT,
                        message = "How are you?",
                        createdBy = WORKER_WITH_CHAT,
                        createdAt = OffsetDateTime.parse("2025-01-12T10:16:30+00:00")
                )
        )
    }
}

private class TestStompSessionHandler : StompSessionHandlerAdapter() {
    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        // Optionally, handle specific frame-level errors here
    }

    override fun handleTransportError(session: StompSession, exception: Throwable) {
        throw RuntimeException("WebSocket transport error", exception)
    }
}

private class TestStompFrameHandler (
        private val queue: BlockingQueue<ChatMessageDTO>
) : StompFrameHandler {
    override fun getPayloadType(headers: StompHeaders): Type = ChatMessageDTO::class.java

    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        queue.add(payload as ChatMessageDTO)
    }
}