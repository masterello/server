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
import com.masterello.chat.dto.InboxItemDTO
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageReadRepository
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
    @Autowired
    private lateinit var chatRepository: ChatRepository
    @Autowired
    private lateinit var messageReadRepository: MessageReadRepository

    @BeforeEach
    fun setup() {
        stompClient = WebSocketStompClient(StandardWebSocketClient())
        val messageConverter = MappingJackson2MessageConverter()
        messageConverter.objectMapper = objectMapper
        stompClient.messageConverter = messageConverter
    }

    @Test
    fun `test new messages received and inbox + read tracking`() {
        val userSession = initTheSessionForUser(USER, listOf(AuthZRole.USER))
        val userQueue = subscribeMessages(userSession)
        val userInbox = subscribeInbox(userSession, USER)


        val workerSession = initTheSessionForUser(WORKER_WITH_CHAT, listOf(AuthZRole.WORKER))
        val workerQueue  = subscribeMessages(workerSession)
        val workerInbox = subscribeInbox(workerSession, WORKER_WITH_CHAT)

        val baseUnreadForWorker = messageReadRepository.countByIdRecipientIdAndReadAtIsNull(WORKER_WITH_CHAT)
        userSession.send("/ws/sendMessage/$CHAT", "Message to worker")

        val messageW1 = workerQueue.poll(1, TimeUnit.SECONDS)
        assertEquals("Message to worker", messageW1.message)
        val messageU1 = userQueue.poll(1, TimeUnit.SECONDS)
        assertEquals("Message to worker", messageU1.message)
        // Chat denorm updated
        val chatAfterUserMsg = chatRepository.findById(CHAT).get()
        assertEquals("Message to worker", chatAfterUserMsg.lastMessagePreview)
        // Inbox events for both participants
        val inboxForWorker1 = workerInbox.poll(1, TimeUnit.SECONDS)
        assertEquals(CHAT, inboxForWorker1.chatId)
        assertEquals("Message to worker", inboxForWorker1.lastMessagePreview)
        assertEquals(USER, inboxForWorker1.senderId)
        val inboxForUser1 = userInbox.poll(1, TimeUnit.SECONDS)
        assertEquals(CHAT, inboxForUser1.chatId)
        assertEquals("Message to worker", inboxForUser1.lastMessagePreview)
        assertEquals(USER, inboxForUser1.senderId)
        // Read tracking: unread for worker increased
        val unreadForWorker = messageReadRepository.countByIdRecipientIdAndReadAtIsNull(WORKER_WITH_CHAT)
        assertEquals(baseUnreadForWorker + 1, unreadForWorker)

        val baseUnreadForUser = messageReadRepository.countByIdRecipientIdAndReadAtIsNull(USER)
        workerSession.send("/ws/sendMessage/$CHAT", "Message to user")
        val messageW2 = workerQueue.poll(1, TimeUnit.SECONDS)
        assertEquals("Message to user", messageW2.message)
        val messageU2 = userQueue.poll(1, TimeUnit.SECONDS)
        assertEquals("Message to user", messageU2.message)
        val chatAfterWorkerMsg = chatRepository.findById(CHAT).get()
        assertEquals("Message to user", chatAfterWorkerMsg.lastMessagePreview)
        val inboxForUser2 = userInbox.poll(1, TimeUnit.SECONDS)
        assertEquals(CHAT, inboxForUser2.chatId)
        assertEquals("Message to user", inboxForUser2.lastMessagePreview)
        assertEquals(WORKER_WITH_CHAT, inboxForUser2.senderId)
        val inboxForWorker2 = workerInbox.poll(1, TimeUnit.SECONDS)
        assertEquals(CHAT, inboxForWorker2.chatId)
        assertEquals("Message to user", inboxForWorker2.lastMessagePreview)
        assertEquals(WORKER_WITH_CHAT, inboxForWorker2.senderId)
        val unreadForUser = messageReadRepository.countByIdRecipientIdAndReadAtIsNull(USER)
        assertEquals(baseUnreadForUser + 1, unreadForUser)
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
                .connectAsync("ws://localhost:$port/ws/chat", WebSocketHttpHeaders(headers), TestStompSessionHandler())
                .get(1, TimeUnit.SECONDS)
    }

    private fun subscribeMessages(session: StompSession): BlockingQueue<ChatMessageDTO> {
        val queue = LinkedBlockingQueue<ChatMessageDTO>();
        session.subscribe("/topic/messages/$CHAT", TestStompFrameHandler(queue))
        return queue;
    }

    private fun subscribeInbox(session: StompSession, userId: UUID): BlockingQueue<InboxItemDTO> {
        val queue = LinkedBlockingQueue<InboxItemDTO>()
        session.subscribe("/topic/inbox/$userId", TestInboxFrameHandler(queue))
        return queue
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

private class TestInboxFrameHandler(
    private val queue: BlockingQueue<InboxItemDTO>
) : StompFrameHandler {
    override fun getPayloadType(headers: StompHeaders): Type = InboxItemDTO::class.java
    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        queue.add(payload as InboxItemDTO)
    }
}
