package com.masterello.chat.service

import com.masterello.auth.data.AuthData
import com.masterello.chat.ChatTestDataProvider.Companion.buildUser
import com.masterello.chat.ChatTestDataProvider.Companion.buildWorker
import com.masterello.chat.domain.Chat
import com.masterello.chat.domain.ChatType
import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatMessageDTO
import com.masterello.chat.mapper.ChatMapper
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageReadRepository
import com.masterello.chat.repository.MessageReadRepository.UnreadPerChat
import com.masterello.chat.repository.MessageRepository
import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.task.dto.TaskDto
import com.masterello.task.dto.TaskStatus
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.user.service.MasterelloUserService
import com.masterello.worker.service.ReadOnlyWorkerService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.OffsetDateTime
import java.util.*

private const val UNREAD_MESSAGES = 5L

class ChatServiceTest {

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var taskService: ReadOnlyTaskService

    @Mock
    private lateinit var workerService: ReadOnlyWorkerService

    @Mock
    private lateinit var messageRepository: MessageRepository

    @Mock
    private lateinit var messageMapper: MessageMapper

    @Mock
    private lateinit var chatMapper: ChatMapper

    @Mock
    private lateinit var userService: MasterelloUserService

    @Mock
    private lateinit var messageReadRepository: MessageReadRepository


    @InjectMocks
    private lateinit var chatService: ChatService

    private val workerId = UUID.randomUUID()
    private val taskId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockSecurityContext(userId)
    }

    private fun mockSecurityContext(requesterId: UUID) {
        // Mocking MasterelloAuthentication and its details property
        val authentication = Mockito.mock(MasterelloAuthentication::class.java)
        val details = Mockito.mock(AuthData::class.java)  // Mock the details property

        // Setting up the mocked details property
        whenever(authentication.details).thenReturn(details)
        whenever(details.userId).thenReturn(requesterId)  // Mock userId inside details

        val securityContext = Mockito.mock(SecurityContext::class.java)
        whenever(securityContext.authentication).thenReturn(authentication)

        SecurityContextHolder.setContext(securityContext)
    }

    // === General Chat Tests ===

    @Test
    fun `getGeneralChat should return existing general chat if found`() {
        val existingChat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findByUserIdAndWorkerIdAndChatType(userId, workerId, ChatType.GENERAL))
                .thenReturn(existingChat)
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(
                mapOf(
                        userId to buildUser(userId, "John", "Doe"),
                        workerId to buildUser(workerId, "Jane", "Worker")
                )
        )
        whenever(messageReadRepository.countByIdRecipientIdAndChatIdAndReadAtIsNull(userId, existingChat.id!!))
                .thenReturn(UNREAD_MESSAGES)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))
        whenever(chatMapper.toDTO(eq(existingChat), any(), eq(UNREAD_MESSAGES))).thenReturn(
                ChatDTO(
                        id = existingChat.id!!,
                        userId = existingChat.userId,
                        workerId = existingChat.workerId,
                        userName = "John Doe",
                        workerName = "Jane Worker",
                        chatType = existingChat.chatType,
                        taskId = existingChat.taskId,
                        createdAt = existingChat.createdAt!!,
                        lastMessageAt = null,
                        lastMessagePreview = null,
                        unreadCount = UNREAD_MESSAGES
                )
        )

        val result = chatService.getGeneralChat(userId, workerId)

        assertEquals(existingChat.id, result.id)
        assertEquals(ChatType.GENERAL, result.chatType)
        assertEquals(userId, result.userId)
        assertEquals(workerId, result.workerId)
        assertEquals(null, result.taskId)
        assertEquals("John Doe", result.userName)
        assertEquals("Jane Worker", result.workerName)

        Mockito.verify(chatRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `createGeneralChatPublic should create new general chat if none exists`() {
        whenever(chatRepository.findByUserIdAndWorkerIdAndChatType(userId, workerId, ChatType.GENERAL))
                .thenReturn(null)
        val chatId = UUID.randomUUID()
        whenever(chatRepository.save(Mockito.any(Chat::class.java))).thenAnswer {
            val chat = it.getArgument<Chat>(0)
            createGeneralChat(chat.userId, chat.workerId, chatId)
        }
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(
                mapOf(
                        userId to buildUser(userId, "John", "Doe"),
                        workerId to buildUser(workerId, "Jane", "Worker")
                )
        )
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))
        whenever(chatMapper.toDTO(any<Chat>(), any(), eq(0))).thenAnswer {
            val chat = it.getArgument<Chat>(0)
            ChatDTO(
                    id = chat.id!!,
                    userId = chat.userId,
                    workerId = chat.workerId,
                    userName = "John Doe",
                    workerName = "Jane Worker",
                    chatType = chat.chatType,
                    taskId = chat.taskId,
                    createdAt = chat.createdAt!!,
                    lastMessageAt = null,
                    lastMessagePreview = null,
                    unreadCount = 0
            )
        }

        val result = chatService.createGeneralChatPublic(userId, workerId)

        assertNotNull(result)
        assertEquals(ChatType.GENERAL, result.chatType)
        assertEquals(userId, result.userId)
        assertEquals(workerId, result.workerId)
        assertEquals(null, result.taskId)

        Mockito.verify(chatRepository).save(Mockito.any(Chat::class.java))
    }

    @Test
    fun `createGeneralChatPublic should throw already exists on race`() {
        val existingChat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findByUserIdAndWorkerIdAndChatType(userId, workerId, ChatType.GENERAL))
                .thenReturn(existingChat)
        whenever(chatRepository.save(Mockito.any(Chat::class.java)))
                .thenThrow(DataIntegrityViolationException("Duplicate key"))
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        assertThrows<com.masterello.chat.exceptions.ChatAlreadyExistsException> {
            chatService.createGeneralChatPublic(userId, workerId)
        }
    }

    @Test
    fun `createGeneralChatPublic should throw already exists when duplicate`() {
        whenever(chatRepository.findByUserIdAndWorkerIdAndChatType(userId, workerId, ChatType.GENERAL))
                .thenReturn(null)
        whenever(chatRepository.save(Mockito.any(Chat::class.java)))
                .thenThrow(DataIntegrityViolationException("Duplicate key"))
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        assertThrows<com.masterello.chat.exceptions.ChatAlreadyExistsException> {
            chatService.createGeneralChatPublic(userId, workerId)
        }
    }

    // === Task Chat Tests ===

    @Test
    fun `getTaskChat should return existing task chat if found`() {
        val task = task(taskId, userId, workerId) // task owner = userId, assigned worker = workerId
        val existingChat = createTaskChat(userId, workerId, taskId)

        whenever(taskService.getTask(taskId)).thenReturn(task)
        whenever(chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(userId, workerId, taskId, ChatType.TASK_SPECIFIC))
                .thenReturn(existingChat)
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(
                mapOf(
                        userId to buildUser(userId, "John", "Doe"),
                        workerId to buildUser(workerId, "Jane", "Worker")
                )
        )
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))
        whenever(chatMapper.toDTO(eq(existingChat), any(), eq(0))).thenReturn(
                ChatDTO(
                        id = existingChat.id!!,
                        userId = existingChat.userId,
                        workerId = existingChat.workerId,
                        userName = "John Doe",
                        workerName = "Jane Worker",
                        chatType = existingChat.chatType,
                        taskId = existingChat.taskId,
                        createdAt = existingChat.createdAt!!,
                        lastMessageAt = null,
                        lastMessagePreview = null,
                        unreadCount = 0
                )
        )

        val result = chatService.getTaskChat(taskId, workerId)

        assertEquals(existingChat.id, result.id)
        assertEquals(ChatType.TASK_SPECIFIC, result.chatType)
        assertEquals(taskId, result.taskId)
        assertEquals(userId, result.userId)
        assertEquals(workerId, result.workerId)

        Mockito.verify(chatRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `createTaskChatPublic should create new task chat if none exists`() {
        val task = task(taskId, userId, workerId) // task owner = userId, assigned worker = workerId

        whenever(taskService.getTask(taskId)).thenReturn(task)
        whenever(chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(userId, workerId, taskId, ChatType.TASK_SPECIFIC))
                .thenReturn(null)
        whenever(chatRepository.save(Mockito.any(Chat::class.java))).thenAnswer {
            val chat = it.getArgument<Chat>(0)
            createTaskChat(chat.userId, chat.workerId, taskId)
        }
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(
                mapOf(
                        userId to buildUser(userId, "John", "Doe"),
                        workerId to buildUser(workerId, "Jane", "Worker")
                )
        )
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))
        whenever(chatMapper.toDTO(any<Chat>(), any(), eq(0))).thenAnswer {
            val chat = it.getArgument<Chat>(0)
            ChatDTO(
                    id = chat.id!!,
                    userId = chat.userId,
                    workerId = chat.workerId,
                    userName = "John Doe",
                    workerName = "Jane Worker",
                    chatType = chat.chatType,
                    taskId = chat.taskId,
                    createdAt = chat.createdAt!!,
                    lastMessageAt = null,
                    lastMessagePreview = null,
                    unreadCount = 0
            )
        }

        val result = chatService.createTaskChatPublic(taskId, workerId)

        assertNotNull(result)
        assertEquals(ChatType.TASK_SPECIFIC, result.chatType)
        assertEquals(taskId, result.taskId)
        assertEquals(userId, result.userId)
        assertEquals(workerId, result.workerId)

        Mockito.verify(chatRepository).save(Mockito.any(Chat::class.java))
    }

    // === Chat History Tests ===

    @Test
    fun `getChatHistory should return message history`() {
        val chatId = UUID.randomUUID()
        val before = OffsetDateTime.now()
        val limit = 10

        val mockMessages = listOf(
                createMockMessage(UUID.randomUUID(), chatId, "Hello", userId),
                createMockMessage(UUID.randomUUID(), chatId, "Hi there", workerId)
        )
        val mockPage = PageImpl(mockMessages)

        whenever(messageRepository.findByChatIdAndCreatedAtBefore(eq(chatId), eq(before), any<PageRequest>()))
                .thenReturn(mockPage)
        whenever(messageMapper.toDto(any(), any())).thenAnswer {
            val message = it.getArgument<com.masterello.chat.domain.Message>(0)
            ChatMessageDTO(
                    id = message.id!!,
                    chatId = message.chatId!!,
                    message = message.message!!,
                    createdBy = message.createdBy!!,
                    createdAt = message.createdAt!!,
            )
        }

        val result = chatService.getChatHistory(chatId, limit, before)

        assertNotNull(result)
        assertEquals(2, result.messages.size)
    }

    // === User Chats Tests ===

    @Test
    fun `getUserChats should return all active chats for user`() {
        val chat1 = createGeneralChat(userId, workerId)
        val chat2 = createTaskChat(userId, UUID.randomUUID(), taskId)

        whenever(chatRepository.findNonEmptyChatsFor(eq(userId), any()))
                .thenReturn(PageImpl(listOf(chat1, chat2), PageRequest.of(0,2), 2))
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(
                mapOf(
                        userId to buildUser(userId, "John", "Doe"),
                        workerId to buildUser(workerId, "Jane", "Worker")
                )
        )
        whenever(userService.findAllByIds(setOf(userId, chat2.workerId))).thenReturn(
                mapOf(
                        userId to buildUser(userId, "John", "Doe"),
                        chat2.workerId to buildUser(chat2.workerId, "Bob", "Builder")
                )
        )
        whenever(messageReadRepository.unreadPerChat(userId))
                .thenReturn(listOf(UnreadPerChatImpl(chat1.id!!, UNREAD_MESSAGES),
                        UnreadPerChatImpl(chat2.id!!, UNREAD_MESSAGES)))
        whenever(chatMapper.toDTO(any<Chat>(), any(), eq(UNREAD_MESSAGES))).thenAnswer {
            val chat = it.getArgument<Chat>(0)
            ChatDTO(
                    id = chat.id!!,
                    userId = chat.userId,
                    workerId = chat.workerId,
                    userName = "John Doe",
                    workerName = if (chat.chatType == ChatType.GENERAL) "Jane Worker" else "Bob Builder",
                    chatType = chat.chatType,
                    taskId = chat.taskId,
                    createdAt = chat.createdAt!!,
                    lastMessageAt = null,
                    lastMessagePreview = null,
                    unreadCount = UNREAD_MESSAGES
            )
        }

        val result = chatService.getUserChats(1,2)

        assertEquals(2, result.items.size)
        assertTrue(result.items.any { it.chatType == ChatType.GENERAL })
        assertTrue(result.items.any { it.chatType == ChatType.TASK_SPECIFIC })
    }

    // === Helper Methods ===

    private fun createGeneralChat(userId: UUID, workerId: UUID, chatId: UUID = UUID.randomUUID()): Chat {
        return Chat(
                id = chatId,
                userId = userId,
                workerId = workerId,
                chatType = ChatType.GENERAL,
                taskId = null,
                createdAt = OffsetDateTime.now(),
        )
    }

    private fun createTaskChat(userId: UUID, workerId: UUID, taskId: UUID): Chat {
        return Chat(
                id = UUID.randomUUID(),
                userId = userId,
                workerId = workerId,
                chatType = ChatType.TASK_SPECIFIC,
                taskId = taskId,
                createdAt = OffsetDateTime.now(),
        )
    }

    private fun createMockMessage(
            id: UUID,
            chatId: UUID,
            messageText: String,
            createdBy: UUID
    ): com.masterello.chat.domain.Message {
        return com.masterello.chat.domain.Message(
                id = id,
                chatId = chatId,
                message = messageText,
                createdBy = createdBy,
                createdAt = OffsetDateTime.now()
        )
    }

    private fun task(taskUuid: UUID, userId: UUID, workerId: UUID): TaskDto {
        return TaskDto(
                uuid = taskUuid,
                userUuid = userId,
                workerUuid = workerId,
                categoryCode = 123,
                name = "Test Task",
                description = "Test task description",
                status = TaskStatus.NEW,
                createdDate = OffsetDateTime.now(),
                updatedDate = OffsetDateTime.now()
        )
    }

    private class UnreadPerChatImpl(var vChatId: UUID, var vUnreadCount: Long) : UnreadPerChat{
        override fun getChatId(): UUID {
            return vChatId
        }

        override fun getUnread(): Long {
            return vUnreadCount
        }

    }
}
