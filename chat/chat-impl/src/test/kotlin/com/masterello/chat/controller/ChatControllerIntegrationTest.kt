package com.masterello.chat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.auth.data.AuthZRole
import com.masterello.auth.extension.AuthMocked
import com.masterello.chat.ChatTestConfiguration
import com.masterello.chat.ChatTestDataProvider.Companion.CHAT
import com.masterello.chat.ChatTestDataProvider.Companion.TASK_ID
import com.masterello.chat.ChatTestDataProvider.Companion.USER
import com.masterello.chat.ChatTestDataProvider.Companion.USER_S
import com.masterello.chat.ChatTestDataProvider.Companion.WORKER
import com.masterello.chat.ChatTestDataProvider.Companion.WORKER_S
import com.masterello.chat.ChatTestDataProvider.Companion.WORKER_WITH_CHAT
import com.masterello.chat.ChatTestDataProvider.Companion.WORKER_WITH_CHAT_S
import com.masterello.chat.ChatTestDataProvider.Companion.buildUser
import com.masterello.chat.ChatTestDataProvider.Companion.buildWorker
import com.masterello.chat.ChatTestDataProvider.Companion.task
import com.masterello.chat.ChatTestDataProvider.Companion.tokenCookie
import com.masterello.chat.domain.ChatType
import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatHistoryDTO
import com.masterello.chat.dto.GetOrCreateGeneralChatDTO
import com.masterello.chat.dto.GetOrCreateTaskChatDTO
import com.masterello.chat.repository.ChatRepository
import com.masterello.commons.test.AbstractWebIntegrationTest
import com.masterello.task.service.ReadOnlyTaskService
import com.masterello.user.service.MasterelloUserService
import com.masterello.worker.service.ReadOnlyWorkerService
import io.restassured.RestAssured
import lombok.SneakyThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SqlGroup(Sql(scripts = ["classpath:sql/create-chat-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["classpath:sql/clean.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ChatTestConfiguration::class])
class ChatControllerIntegrationTest : AbstractWebIntegrationTest() {


    @Autowired
    private lateinit var taskService: ReadOnlyTaskService
    @Autowired
    private lateinit var workerService: ReadOnlyWorkerService
    @Autowired
    private lateinit var userService: MasterelloUserService
    @Autowired
    private lateinit var chatRepository: ChatRepository
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Test
    @AuthMocked(userId = USER_S, roles = [AuthZRole.USER])
    fun `test chat created by task owner when chat doesn't exist`() {
       
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.of(buildWorker(WORKER)))
        whenever(userService.findAllByIds(setOf( USER, WORKER)))
                .thenReturn(mapOf(Pair(USER, buildUser(USER, "Harry", "Potter")), 
                        Pair(WORKER, buildUser(WORKER, "Hagrid", "Hagridson"))))
        whenever(taskService.getTask(TASK_ID)).thenReturn(task)
        val chatBefore = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNull(chatBefore)
        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER
         )

        //@formatter:off

            val response = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(chat)
                .`when`()
                    .post("/api/chat/task")
                .then()
                    .statusCode(200)
        //@formatter:on

        val chatDTO = response.extract().body().`as`(ChatDTO::class.java)
        assertNotNull(chatDTO)
        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNotNull(chatCreated)
        assertEquals(chatDTO.taskId, chatCreated.taskId)
        assertEquals(USER, chatDTO.userId)
        assertEquals("Harry Potter", chatDTO.userName)
        assertEquals(WORKER, chatDTO.workerId)
        assertEquals("Hagrid Hagridson", chatDTO.workerName)
        assertEquals(TASK_ID, chatDTO.taskId)
    }

    @Test
    @AuthMocked(userId = WORKER_S, roles = [AuthZRole.WORKER])
    fun `test chat created by task worker when chat doesn't exist`() {
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.of(buildWorker(WORKER)))
        whenever(userService.findAllByIds(setOf( USER, WORKER)))
                .thenReturn(mapOf(Pair(USER, buildUser(USER, "Harry", "Potter")),
                        Pair(WORKER, buildUser(WORKER, "Hagrid", "Hagridson"))))
        whenever(taskService.getTask(TASK_ID)).thenReturn(task)
        val chatBefore = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNull(chatBefore)
        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER
        )

        //@formatter:off

        val response = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(chat)
                .`when`()
                    .post("/api/chat/task")
                .then()
                    .statusCode(200)
        //@formatter:on

        val chatDTO = response.extract().body().`as`(ChatDTO::class.java)
        assertNotNull(chatDTO)
        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNotNull(chatCreated)
        assertEquals(chatDTO.taskId, chatCreated.taskId)
        assertEquals(USER, chatDTO.userId)
        assertEquals("Harry Potter", chatDTO.userName)
        assertEquals(WORKER, chatDTO.workerId)
        assertEquals("Hagrid Hagridson", chatDTO.workerName)
        assertEquals(TASK_ID, chatDTO.taskId)
    }

    @Test
    @AuthMocked(userId = USER_S, roles = [AuthZRole.USER])
    fun `test chat request fails when user with workerId doesn't exist`() {
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.empty())

        whenever(taskService.getTask(TASK_ID)).thenReturn(task)
        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER
        )

        //@formatter:off

        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(chat)
                .`when`()
                    .post("/api/chat/task")
                .then()
                    .statusCode(404)
        //@formatter:on

        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNull(chatCreated)
    }

    @Test
    @AuthMocked(userId = "8824a15c-98f5-49d9-bd97-43d1cba3f62c", roles = [AuthZRole.USER])
    fun `test chat request fails when user is neither owner nor worker`() {
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.of(buildWorker(WORKER)))

        whenever(taskService.getTask(TASK_ID)).thenReturn(task)
        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER
        )

        //@formatter:off

        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(chat)
                .`when`()
                    .post("/api/chat/task")
                .then()
                    .statusCode(403)
        //@formatter:on

        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNull(chatCreated)
    }

    @Test
    @AuthMocked(userId = WORKER_S, roles = [AuthZRole.WORKER])
    fun `test creeate chat by worker when task doesn't exist`() {
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.of(buildWorker(WORKER)))

        whenever(taskService.getTask(TASK_ID)).thenReturn(null)
        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER
        )

        //@formatter:off

        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(chat)
                .`when`()
                    .post("/api/chat/task")
                .then()
                    .statusCode(403)
        //@formatter:on

        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNull(chatCreated)
    }

    @Test
    fun `test chat request fails for anonymous user`() {

        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER
        )

        //@formatter:off

        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(chat)
                .`when`()
                    .post("/api/chat/task")
                .then()
                    .statusCode(401)
        //@formatter:on

        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNull(chatCreated)
    }

    @Test
    @AuthMocked(userId = USER_S, roles = [AuthZRole.USER])
    fun `test existing chat fetched by task owner`() {
        whenever(workerService.getWorkerInfo(WORKER_WITH_CHAT)).thenReturn(Optional.of(buildWorker(WORKER_WITH_CHAT)))
        whenever(userService.findAllByIds(setOf(USER, WORKER_WITH_CHAT)))
                .thenReturn(mapOf(Pair(USER, buildUser(USER, "Harry", "Potter")),
                        Pair(WORKER_WITH_CHAT, buildUser(WORKER_WITH_CHAT, "Hagrid", "Hagridson"))))
        whenever(taskService.getTask(TASK_ID)).thenReturn(task)
        val chatBefore = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER_WITH_CHAT, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNotNull(chatBefore)

        //@formatter:off

        val response = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .`when`()
                    .get("/api/chat/task?taskId=$TASK_ID&workerId=$WORKER_WITH_CHAT")
                .then()
                    .statusCode(200)
        //@formatter:on

        val chatDTO = response.extract().body().`as`(ChatDTO::class.java)
        assertNotNull(chatDTO)
        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER_WITH_CHAT, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNotNull(chatCreated)
        assertEquals(chatDTO.taskId, chatCreated.taskId)
        assertEquals(USER, chatDTO.userId)
        assertEquals("Harry Potter", chatDTO.userName)
        assertEquals(WORKER_WITH_CHAT, chatDTO.workerId)
        assertEquals("Hagrid Hagridson", chatDTO.workerName)
        assertEquals(TASK_ID, chatDTO.taskId)
    }

    @Test
    @AuthMocked(userId = WORKER_WITH_CHAT_S, roles = [AuthZRole.WORKER])
    fun `test existing chat fetched by worker`() {
        whenever(workerService.getWorkerInfo(WORKER_WITH_CHAT)).thenReturn(Optional.of(buildWorker(WORKER_WITH_CHAT)))
        whenever(userService.findAllByIds(setOf(USER, WORKER_WITH_CHAT)))
                .thenReturn(mapOf(Pair(USER, buildUser(USER, "Harry", "Potter")),
                        Pair(WORKER_WITH_CHAT, buildUser(WORKER_WITH_CHAT, "Hagrid", "Hagridson"))))
        whenever(taskService.getTask(TASK_ID)).thenReturn(task)
        val chatBefore = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER_WITH_CHAT, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNotNull(chatBefore)
        val chat= GetOrCreateTaskChatDTO(
                taskId = TASK_ID,
                workerId = WORKER_WITH_CHAT
        )

        //@formatter:off

        val response = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .`when`()
                    .get("/api/chat/task?taskId=$TASK_ID&workerId=$WORKER_WITH_CHAT")
                .then()
                    .statusCode(200)
        //@formatter:on

        val chatDTO = response.extract().body().`as`(ChatDTO::class.java)
        assertNotNull(chatDTO)
        val chatCreated = chatRepository.findByUserIdAndWorkerIdAndTaskIdAndChatType(USER, WORKER_WITH_CHAT, TASK_ID, ChatType.TASK_SPECIFIC)
        assertNotNull(chatCreated)
        assertEquals(chatDTO.taskId, chatCreated.taskId)
        assertEquals(USER, chatDTO.userId)
        assertEquals("Harry Potter", chatDTO.userName)
        assertEquals(WORKER_WITH_CHAT, chatDTO.workerId)
        assertEquals("Hagrid Hagridson", chatDTO.workerName)
        assertEquals(TASK_ID, chatDTO.taskId)
    }

    // === General Chat Tests ===
    
    @Test
    @AuthMocked(userId = USER_S, roles = [AuthZRole.USER])
    fun `test general chat creation by user`() {
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.of(buildWorker(WORKER)))
        whenever(userService.findAllByIds(setOf(USER, WORKER)))
            .thenReturn(mapOf(
                USER to buildUser(USER, "Harry", "Potter"),
                WORKER to buildUser(WORKER, "Hagrid", "Hagridson")
            ))

        //@formatter:off
        val response = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(tokenCookie())
                .with().body(GetOrCreateGeneralChatDTO(WORKER, USER))
            .`when`()
                .post("/api/chat")
            .then()
                .statusCode(200)
        //@formatter:on

        val chatDTO = response.extract().body().`as`(ChatDTO::class.java)
        assertNotNull(chatDTO)
        assertEquals("GENERAL", chatDTO.chatType.name)
        assertNull(chatDTO.taskId)
        assertEquals(USER, chatDTO.userId)
        assertEquals(WORKER, chatDTO.workerId)
        assertEquals("Harry Potter", chatDTO.userName)
        assertEquals("Hagrid Hagridson", chatDTO.workerName)
    }
    
    @Test
    @AuthMocked(userId = WORKER_S, roles = [AuthZRole.WORKER]) 
    fun `test general chat creation by worker`() {
        whenever(workerService.getWorkerInfo(WORKER)).thenReturn(Optional.of(buildWorker(WORKER)))
        whenever(userService.findAllByIds(setOf(WORKER, USER)))
            .thenReturn(mapOf(
                WORKER to buildUser(WORKER, "Hagrid", "Hagridson"),
                USER to buildUser(USER, "Harry", "Potter")
            ))

        //@formatter:off
        val response = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(tokenCookie())
                .with().body(GetOrCreateGeneralChatDTO(WORKER, USER))
            .`when`()
                .post("/api/chat")
            .then()
                .statusCode(200)
        //@formatter:on

        val chatDTO = response.extract().body().`as`(ChatDTO::class.java)
        assertNotNull(chatDTO)
        assertEquals("GENERAL", chatDTO.chatType.name)
        assertNull(chatDTO.taskId)
        assertEquals(WORKER, chatDTO.workerId) // Requester becomes "user"
        assertEquals(USER, chatDTO.userId) // Other participant becomes "worker"
    }

    @Test
    fun `test general chat creation fails for anonymous user`() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .with().body(GetOrCreateGeneralChatDTO(WORKER, USER))
            .`when`()
                .post("/api/chat")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER_S, roles = [AuthZRole.USER])
    fun `test get user chats`() {
        whenever(userService.findAllByIds(setOf(WORKER_WITH_CHAT, USER)))
                .thenReturn(mapOf(
                        WORKER_WITH_CHAT to buildUser(WORKER_WITH_CHAT, "Hagrid", "Hagridson"),
                        USER to buildUser(USER, "Harry", "Potter")
                ))

        //@formatter:off
        val response = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(tokenCookie())
            .`when`()
                .get("/api/chat/my-chats")
            .then()
                .statusCode(200)
        //@formatter:on

        val page = response.extract().body().`as`(com.masterello.chat.dto.ChatPageDTO::class.java)
        assertNotNull(page)
        // Should contain items from test data
        assertNotNull(page.items)
    }

    @ParameterizedTest
    @CsvSource("NULL, chat-history-1.json", "2025-01-12T10:17:00+00:00, chat-history-2.json")
    @AuthMocked(userId = WORKER_WITH_CHAT_S, roles = [AuthZRole.WORKER])
    fun `test fetch history chat fetched by worker`(beforeStr: String, responseFile: String) {
        val before = if(beforeStr == "NULL") {
            null
        } else beforeStr

        //@formatter:off
        val response = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("before", before)
                    .cookie(tokenCookie())
                .`when`()
                    .get("/api/chat/$CHAT/history")
                .then()
                    .statusCode(200)
        //@formatter:on
        //@formatter:off
        val filePath = String.format("src/test/resources/responses/%s", responseFile)
        val expectedResponse: ChatHistoryDTO = readHistoryFromFile(filePath)
        val actualResponse = response.extract().body().`as`(ChatHistoryDTO::class.java)
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    @AuthMocked(userId = WORKER_WITH_CHAT_S, roles = [AuthZRole.WORKER])
    fun `test fetch history chat not found`() {
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .`when`()
                    .get("/api/chat/${UUID.randomUUID()}/history")
                .then()
                    .statusCode(403)
        //@formatter:on
    }

    @SneakyThrows fun readHistoryFromFile(filePath:String?): ChatHistoryDTO {
        return objectMapper.readValue(File(filePath), ChatHistoryDTO::class.java)
    }
}