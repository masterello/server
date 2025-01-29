package com.masterello.chat.service

import com.masterello.auth.data.AuthData
import com.masterello.chat.ChatTestDataProvider.Companion.buildUser
import com.masterello.chat.ChatTestDataProvider.Companion.buildWorker
import com.masterello.chat.domain.Chat
import com.masterello.chat.exceptions.ChatCreationValidationException
import com.masterello.chat.exceptions.TaskNotFoundException
import com.masterello.chat.exceptions.UserNotFoundException
import com.masterello.chat.mapper.ChatMapper
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageRepository
import com.masterello.commons.security.data.MasterelloAuthentication
import com.masterello.commons.security.exception.UnauthorisedException
import com.masterello.task.dto.TaskDto
import com.masterello.task.dto.TaskStatus
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.user.service.MasterelloUserService
import com.masterello.worker.service.ReadOnlyWorkerService
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.sql.SQLException
import java.util.*

class ChatServiceTest {

    @Mock
    private lateinit var repository: ChatRepository

    @Mock
    private lateinit var taskService: ReadOnlyTaskService

    @Mock
    private lateinit var workerService: ReadOnlyWorkerService

    @Mock
    private lateinit var messageRepository: MessageRepository

    @Mock
    private lateinit var messageMapper: MessageMapper

    @Spy
    private lateinit var chatMapper: ChatMapper

    @Mock
    private lateinit var userService: MasterelloUserService

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

    @Test
    fun `getOrCreateChat should return existing chat if found`() {
        val existingChat = Chat(id = UUID.randomUUID(), taskId = taskId, workerId = workerId, userId = userId)
        whenever(repository.findByWorkerIdAndTaskId(workerId, taskId)).thenReturn(existingChat)
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(mapOf(
                Pair(userId, buildUser(userId, "Name", "Lastname")),
                Pair(workerId, buildUser(workerId, "WName", "WLastname"))
        ))
        val taskDto = task(taskId, userId, workerId)
        whenever(taskService.getTask(taskId)).thenReturn(taskDto)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        val chat = chatService.getOrCreateChat(workerId, taskId)

        assertEquals(existingChat.id, chat.id)
        assertEquals(existingChat.taskId, chat.taskId)
        assertEquals(existingChat.userId, chat.userId)
        assertEquals(existingChat.workerId, chat.workerId)
        assertEquals("Name Lastname", chat.userName)
        assertEquals("WName WLastname", chat.workerName)

        Mockito.verify(repository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `getOrCreateChat should create and return new chat if none exists`() {
        val taskDto = task(taskId, userId, workerId)
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(mapOf(
                Pair(userId, buildUser(userId, "Name", "Lastname")),
                Pair(workerId, buildUser(workerId, "WName", "WLastname"))
        ))
        whenever(repository.findByWorkerIdAndTaskId(workerId, taskId)).thenReturn(null)
        whenever(taskService.getTask(taskId)).thenReturn(taskDto)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))
        whenever(repository.save(Mockito.any(Chat::class.java))).thenAnswer {
            val chat = it.getArgument<Chat>(0)
            Chat(UUID.randomUUID(), chat.taskId, chat.userId, chat.workerId)
        }

        val chat = chatService.getOrCreateChat(workerId, taskId)

        assertNotNull(chat)
        assertEquals(taskId, chat.taskId)
        assertEquals(workerId, chat.workerId)
        assertEquals(userId, chat.userId)
    }

    @Test
    fun `getOrCreateChat should throw TaskNotFoundException if task not found`() {
        whenever(taskService.getTask(taskId)).thenReturn(null)

        assertThrows<TaskNotFoundException> {
            chatService.getOrCreateChat(workerId, taskId)
        }
    }

    @Test
    fun `getOrCreateChat should throw UserNotFoundException if worker not found`() {
        whenever(taskService.getTask(taskId)).thenReturn(task(taskId, userId, workerId))
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            chatService.getOrCreateChat(workerId, taskId)
        }
    }

    @Test
    fun `getOrCreateChat should throw UnauthorisedException if requester is not authorized`() {
        val taskDto = task(taskId, UUID.randomUUID(), workerId)
        whenever(taskService.getTask(taskId)).thenReturn(taskDto)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))

        assertThrows<UnauthorisedException> {
            chatService.getOrCreateChat(workerId, taskId)
        }
    }

    @Test
    fun `createChat should throw ChatCreationValidationException if constraint violation occurs`() {
        val taskDto = task(taskId, userId, workerId)
        whenever(userService.findAllByIds(setOf(userId, workerId))).thenReturn(mapOf(
                Pair(userId, buildUser(userId, "Name", "Lastname")),
                Pair(workerId, buildUser(workerId, "WName", "WLastname"))
        ))
        whenever(taskService.getTask(taskId)).thenReturn(taskDto)
        whenever(workerService.getWorkerInfo(workerId)).thenReturn(Optional.of(buildWorker(workerId)))
        whenever(repository.save(Mockito.any(Chat::class.java)))
                .thenThrow(DataIntegrityViolationException("Duplicated chat"))

        assertThrows<ChatCreationValidationException> {
            chatService.getOrCreateChat(workerId, taskId)
        }
    }

    private fun task(taskUuid: UUID, userId: UUID, workerId: UUID) :TaskDto {
        return TaskDto(
                uuid = taskUuid,
                userUuid = userId,
                workerUuid = workerId,
                categoryCode = 123,
                name = "name",
                description = "description",
                status = TaskStatus.NEW,
                createdDate = null,
                updatedDate = null
        )
    }
}
