package com.masterello.chat.security

import com.masterello.chat.ChatTestDataProvider.Companion.buildWorker
import com.masterello.chat.domain.Chat
import com.masterello.chat.domain.ChatType
import com.masterello.chat.repository.ChatRepository
import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.task.dto.TaskDto
import com.masterello.task.dto.TaskStatus
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.worker.service.ReadOnlyWorkerService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.OffsetDateTime
import java.util.*

class ChatSecurityExpressionsTest {

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var taskService: ReadOnlyTaskService

    @Mock
    private lateinit var workerService: ReadOnlyWorkerService

    @InjectMocks
    private lateinit var chatSecurityExpressions: ChatSecurityExpressions

    private val userId = UUID.randomUUID()
    private val workerId = UUID.randomUUID()
    private val taskId = UUID.randomUUID()
    private val chatId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    // === General Chat Authorization Tests ===

    @Test
    fun `canCreateGeneralChat should allow valid user-worker combination`() {
        // Given
        mockSecurityContext(userId)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        // When
        val result = chatSecurityExpressions.canCreateGeneralChat(userId, workerId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canCreateGeneralChat should reject self-chat attempt`() {
        // Given
        whenever(workerService.getWorkerInfo(userId)).thenReturn(Optional.of(buildWorker(userId)))

        // When
        val result = chatSecurityExpressions.canCreateGeneralChat(userId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `canCreateGeneralChat should reject if worker not found`() {
        // Given
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.empty())

        // When
        val result = chatSecurityExpressions.canCreateGeneralChat(userId, workerId)

        // Then
        assertFalse(result)
    }

    // === Chat Access Authorization Tests ===

    @Test
    fun `canAccessChat should allow chat participants - user`() {
        // Given
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canAccessChat(userId, chatId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canAccessChat should allow chat participants - worker`() {
        // Given
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canAccessChat(workerId, chatId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canAccessChat should reject non-participants`() {
        // Given
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canAccessChat(otherUserId, chatId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `canAccessChat should reject if chat not found`() {
        // Given
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.empty())

        // When
        val result = chatSecurityExpressions.canAccessChat(userId, chatId)

        // Then
        assertFalse(result)
    }

    // === Message Sending Authorization Tests ===

    @Test
    fun `canSendMessageToChat should allow active chat participants`() {
        // Given
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canSendMessageToChat(userId, chatId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canSendMessageToChat should reject non-participants`() {
        // Given
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canSendMessageToChat(otherUserId, chatId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `canSendMessageToChat should reject if chat not found`() {
        // Given
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.empty())

        // When
        val result = chatSecurityExpressions.canSendMessageToChat(userId, chatId)

        // Then
        assertFalse(result)
    }

    // === WebSocket Subscription Authorization Tests ===

    @Test
    fun `canSubscribeToChat should delegate to canAccessChat`() {
        // Given
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canSubscribeToChat(userId, chatId)

        // Then
        assertTrue(result)
    }

    // === SecurityContextHolder Integration Tests ===

    @Test
    fun `canCreateGeneralChat with SecurityContext should work for valid scenario`() {
        // Given
        mockSecurityContext(userId)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        // When
        val result = chatSecurityExpressions.canCreateGeneralChat(userId, workerId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canAccessChat with SecurityContext should work for valid scenario`() {
        // Given
        mockSecurityContext(userId)
        val chat = createGeneralChat(userId, workerId)
        whenever(chatRepository.findById(chatId)).thenReturn(Optional.of(chat))

        // When
        val result = chatSecurityExpressions.canAccessChat(chatId)

        // Then
        assertTrue(result)
    }

    // === Task Chat Tests ===

    @Test
    fun `canCreateTaskChat should allow task owner`() {
        // Given
        mockSecurityContext(userId)
        val task = createTaskDto(taskId, userId, workerId)
        whenever(taskService.getTask(taskId)).thenReturn(task)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        // When
        val result = chatSecurityExpressions.canCreateTaskChat(taskId, workerId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canCreateTaskChat should allow assigned worker`() {
        // Given
        mockSecurityContext(workerId)
        val task = createTaskDto(taskId, otherUserId, workerId)
        whenever(taskService.getTask(taskId)).thenReturn(task)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        // When
        val result = chatSecurityExpressions.canCreateTaskChat(taskId, workerId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canCreateTaskChat should reject unauthorized user`() {
        // Given
        mockSecurityContext(otherUserId)
        val task = createTaskDto(taskId, userId, workerId)
        whenever(taskService.getTask(taskId)).thenReturn(task)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        // When
        val result = chatSecurityExpressions.canCreateTaskChat(taskId, workerId)

        // Then
        assertFalse(result)
    }

    // === Helper Methods ===

    private fun createTaskDto(uuid: UUID, userUuid: UUID, workerUuid: UUID): TaskDto {
        return TaskDto(
            uuid = uuid,
            userUuid = userUuid,
            workerUuid = workerUuid,
            categoryCode = 123,
            name = "Test Task",
            description = "Test Description",
            status = TaskStatus.NEW,
            createdDate = OffsetDateTime.now(),
            updatedDate = OffsetDateTime.now()
        )
    }

    private fun createGeneralChat(userId: UUID, workerId: UUID): Chat {
        return Chat(
            id = chatId,
            userId = userId,
            workerId = workerId,
            chatType = ChatType.GENERAL,
            taskId = null,
            createdAt = OffsetDateTime.now(),
        )
    }

    private fun mockSecurityContext(userId: UUID) {
        val authentication = org.mockito.Mockito.mock(MasterelloAuthentication::class.java)
        val details = org.mockito.Mockito.mock(com.masterello.auth.data.AuthData::class.java)
        
        whenever(authentication.details).thenReturn(details)
        whenever(details.userId).thenReturn(userId)
        
        val securityContext = org.mockito.Mockito.mock(SecurityContext::class.java)
        whenever(securityContext.authentication).thenReturn(authentication)
        
        SecurityContextHolder.setContext(securityContext)
    }
}
