package com.masterello.task.controller

import com.masterello.auth.data.AuthZRole
import com.masterello.auth.extension.AuthMocked
import com.masterello.commons.test.AbstractWebIntegrationTest
import com.masterello.task.TaskTestConfiguration
import com.masterello.task.dto.*
import com.masterello.task.util.TestDataUtils
import com.masterello.task.util.TestDataUtils.TASK_UUID
import com.masterello.task.util.TestDataUtils.TASK_UUID2
import com.masterello.task.util.TestDataUtils.USER1
import com.masterello.task.util.TestDataUtils.USER2
import com.masterello.task.util.TestDataUtils.WORKER1
import com.masterello.task.util.TestDataUtils.WORKER2
import com.masterello.worker.service.ReadOnlyWorkerService
import com.masterello.worker.value.Worker
import io.restassured.RestAssured
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.time.Instant
import java.util.*

@SqlGroup(
    Sql(scripts = ["classpath:sql/create-task-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["classpath:sql/clean.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [TaskTestConfiguration::class])
class TaskControllerIntegrationTest: AbstractWebIntegrationTest() {

    val BASE_URL: String = "/api/tasks"

    @MockBean
    private lateinit var readOnlyWorkerService: ReadOnlyWorkerService

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun getTask() {
        //@formatter:off
        val task = RestAssured
            .given()
                .cookie(TestDataUtils.tokenCookie())
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .get("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals("a45fb214-7c41-4a3d-a990-b499577d46c0", task.uuid.toString())
        assertEquals("0c018736-49e5-4611-8722-d2ecd0567fb1", task.userUuid.toString())
        assertEquals("e5fcf8dd-b6be-4a36-a85a-e2d952cc6254", task.workerUuid.toString())
        assertEquals("Repair the door", task.name)
        assertEquals("Remove old door and put new one", task.description)
        assertEquals(1, task.categoryCode)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, task.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun task_not_found() {
        //@formatter:off
        RestAssured
            .given()
            .cookie(TestDataUtils.tokenCookie())
            .accept("application/json")
            .contentType("application/json")
            .`when`()
            .get("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c9")
            .then()
            .statusCode(404)
        //@formatter:on
    }


    @Test
    fun task_for_non_logged_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .get("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    fun amount_of_completed_worker_tasks_non_logged() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .get("$BASE_URL/worker/0c018736-49e5-4611-8722-d2ecd0567fb1/completed/count")
            .then()
                .statusCode(200)
                .extract().body().`as`(String::class.java)

        assertEquals("0", result)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun amount_of_completed_worker() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .get("$BASE_URL/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/completed/count")
            .then()
                .statusCode(200)
                .extract().body().`as`(String::class.java)

        assertEquals("2", result)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun user_task_search_no_tasks() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/user/769c2113-8b6a-4dfb-8549-fd3de527e226/search")
            .then()
                .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(0, result.totalPages)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(0, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun user_task_search() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        val result = RestAssured
            .given()
                .cookie(TestDataUtils.tokenCookie())
                .accept("application/json")
                .contentType("application/json")
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/user/0c018736-49e5-4611-8722-d2ecd0567fb1/search")
            .then()
                .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(1, result.totalPages)
        assertEquals(7, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(7, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun user_task_search_from_worker_role() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
       RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/user/769c2113-8b6a-4dfb-8549-fd3de527e226/search")
            .then()
                .statusCode(403)

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun user_task_search_no_cookie() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/user/769c2113-8b6a-4dfb-8549-fd3de527e226/search")
            .then()
                .statusCode(401)

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.WORKER])
    fun worker_task_search_no_tasks() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/769c2113-8b6a-4dfb-8549-fd3de527e226/search")
            .then()
                .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(0, result.totalPages)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(0, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun worker_task_search() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        val result = RestAssured
                .given()
                .cookie(TestDataUtils.tokenCookie())
                .accept("application/json")
                .contentType("application/json")
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/search")
            .then()
                .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(1, result.totalPages)
        assertEquals(4, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(4, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.USER])
    fun worker_task_search_from_user_role() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/769c2113-8b6a-4dfb-8549-fd3de527e226/search")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.USER])
    fun worker_task_search_no_cookie() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/user/769c2113-8b6a-4dfb-8549-fd3de527e226/search")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.ADMIN, roles = [AuthZRole.ADMIN])
    fun admin_task_search() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        val result = RestAssured
            .given()
                .cookie(TestDataUtils.tokenCookie())
                .accept("application/json")
                .contentType("application/json")
            .body(taskDtoRequest)
            .`when`()
            .post("$BASE_URL/search")
                .then()
            .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(1, result.totalPages)
        assertEquals(7, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(7, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun admin_task_search_from_user_role() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/search")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.ADMIN, roles = [AuthZRole.ADMIN])
    fun admin_task_search_no_cookie() {
        val taskDtoRequest = TaskDtoRequest()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/search")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun worker_open_task_search_no_tasks() {
        val taskDtoRequest = TaskDtoRequest(categoryCodes = listOf(1,3,4))

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/search")
            .then()
                .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(0, result.totalPages)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(0, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun worker_open_task_search() {
        val taskDtoRequest = TaskDtoRequest(categoryCodes = listOf(1,2,3,4))

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/search")
            .then()
                .statusCode(200)
                .extract().body().`as`(PageOfTaskDto::class.java)

        assertEquals(1, result.totalPages)
        assertEquals(1, result.totalElements)
        assertEquals(0, result.currentPage)
        assertEquals(1, result.tasks.size)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun worker_open_task_search_from_user_role() {
        val taskDtoRequest = TaskDtoRequest(categoryCodes = listOf(1,2,3,4))

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/search")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun worker_open_task_search_no_cookie() {
        val taskDtoRequest = TaskDtoRequest(categoryCodes = listOf(1,2,3,4))

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(taskDtoRequest)
            .`when`()
                .post("$BASE_URL/worker/search")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun create_task_no_cookie() {
        val taskDto = TestDataUtils.createTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(taskDto)
            .`when`()
                .post(BASE_URL)
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun create_task_from_worker() {
        val taskDto = TestDataUtils.createTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post(BASE_URL)
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun create_task_to_another_user() {
        val taskDto = TestDataUtils.createTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post(BASE_URL)
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun create_task_without_worker() {
        val userUuid = UUID.fromString(USER1)
        val taskDto = TestDataUtils.createTaskDto(userUuid = userUuid)

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post(BASE_URL)
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertNull(result.workerUuid)
        assertEquals(TaskStatus.NEW, result.status)
        assertEquals(userUuid, result.userUuid)
        assertEquals("sample task", result.name)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun create_task_with_worker_not_exists() {
        val userUuid = UUID.fromString(USER1)
        val taskDto = TestDataUtils.createTaskDto(userUuid = userUuid, workerUuid = UUID.randomUUID())

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post(BASE_URL)
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun create_task_with_worker() {
        val userUuid = UUID.fromString(USER1)
        val workerUuid = UUID.randomUUID()
        val taskDto = TestDataUtils.createTaskDto(userUuid = userUuid, workerUuid = workerUuid)

        val workerMock = mock(Worker::class.java).apply {
            `when`(workerId).thenReturn(UUID.fromString("e5fcf8dd-b6be-4a36-a85a-e2d952cc6254"))
            `when`(description).thenReturn("best plumber")
            `when`(phone).thenReturn("+49111111111")
            `when`(telegram).thenReturn("plumber-t")
            `when`(whatsapp).thenReturn("plumber-w")
            `when`(viber).thenReturn("plumber-v")
            `when`(isActive).thenReturn(true)
            `when`(registeredAt).thenReturn(Instant.parse("2024-12-14T15:00:00.111111Z"))
        }

        `when`(readOnlyWorkerService.getWorkerInfo(workerUuid))
            .thenReturn(Optional.of(workerMock))
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post(BASE_URL)
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(workerUuid, result.workerUuid)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, result.status)
        assertEquals(userUuid, result.userUuid)
        assertEquals("sample task", result.name)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun update_task_no_cookie() {
        val taskDto = TestDataUtils.updateTaskDto()

        //@formatter:off
        RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .body(taskDto)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/update")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun update_task_from_worker() {
        val taskDto = TestDataUtils.updateTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/update")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun update_task_not_exists() {
        val taskDto = TestDataUtils.updateTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post("$BASE_URL/a45fb214-9999-4a3d-a990-b499577d46c0/update")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun update_task_from_another_user() {
        val taskDto = TestDataUtils.updateTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/update")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun update_finished_task() {
        val taskDto = TestDataUtils.updateTaskDto()

        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/update")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun update_task() {
        val taskDto = TestDataUtils.updateTaskDto(name = "updated task", description = "updated description", categoryCode = 1000)

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(taskDto)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/update")
            .then()
                .statusCode(200)
            .extract().body().`as`(TaskDto::class.java)

        assertEquals(UUID.fromString(USER2), result.userUuid)
        assertEquals(1000, result.categoryCode)
        assertEquals("updated task", result.name)
        assertEquals("updated description", result.description)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun assign_task_no_cookie() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/assign")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun assign_task_from_worker() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/assign")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun assign_task_not_exists() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-9999-4a3d-a990-b499577d46c0/assign")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun assign_task_from_another_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c1/assign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun assign_finished_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/assign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun assign_already_assigned_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/assign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun assign_task() {
        val workerUuid = UUID.randomUUID()

        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", workerUuid)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c1/assign")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(UUID.fromString(USER2), result.userUuid)
        assertEquals(workerUuid, result.workerUuid)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun unassign_task_no_cookie() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/unassign")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun unassign_task_from_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/unassign")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun unassign_task_not_exists() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-9999-4a3d-a990-b499577d46c0/unassign")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun unassign_task_from_another_worker() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/unassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun unassign_finished_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/unassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun unassign_not_assigned_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c1/unassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun unassign_in_progress_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c5/unassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun unassign_task() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/unassign")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)


        assertEquals(TaskStatus.NEW, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun reassign_task_no_cookie() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/reassign")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun reassign_task_from_worker() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/reassign")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun reassign_task_not_exists() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-9999-4a3d-a990-b499577d46c0/reassign")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun reassign_task_from_another_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c1/reassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun reassign_finished_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/reassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun reassign_new_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c2/reassign")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun reassign_same_worker_to_task() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/reassign")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(UUID.fromString(WORKER1), result.workerUuid)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun reassign_worker_to_task_without_worker() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER1)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c1/reassign")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(UUID.fromString(WORKER1), result.workerUuid)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun reassign_worker_to_task() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .queryParam("workerUuid", WORKER2)
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c1/reassign")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(UUID.fromString(WORKER2), result.workerUuid)
        assertEquals(TaskStatus.ASSIGNED_TO_WORKER, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun complete_task_no_cookie() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/complete")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun complete_task_from_another_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/complete")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.WORKER])
    fun complete_task_from_another_worker() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/complete")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun complete_task_not_exists() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
            .post("$BASE_URL/a45fb214-9999-4a3d-a990-b499577d46c0/complete")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun complete_finished_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/complete")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun complete_new_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c2/complete")
            .then()
                .statusCode(400)
        //@formatter:on
    }
    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun complete_task() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c5/complete")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(TaskStatus.IN_REVIEW, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun cancel_task_no_cookie() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/cancel")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = TestDataUtils.USER_NO_TASKS, roles = [AuthZRole.USER])
    fun cancel_task_from_another_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/cancel")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun cancel_task_not_exists() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
            .post("$BASE_URL/a45fb214-9999-4a3d-a990-b499577d46c0/cancel")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun cancel_finished_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/cancel")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun cancel_task() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/cancel")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(TaskStatus.CANCELLED, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun confirm_task_no_cookie() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun confirm_task_from_user() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun confirm_task_not_exists() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-9999-a990-b499577d46c0/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun confirm_task_from_another_worker() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun confirm_finished_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c3/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun confirm_in_review_task() {
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c4/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun confirm_task() {
        //@formatter:off
        val result = RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/a45fb214-7c41-4a3d-a990-b499577d46c0/worker/e5fcf8dd-b6be-4a36-a85a-e2d952cc6254/confirm")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)


        assertEquals(TaskStatus.IN_PROGRESS, result.status)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun review_task_worker_no_cookie() {
        val reviewDto = TestDataUtils.getReviewDto()
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/worker-review")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun review_task_worker_from_user() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(reviewDto)
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/worker-review")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun review_task_worker_not_owner() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/worker-review")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun review_task_worker_task_not_exists() {
        val reviewDto = TestDataUtils.getReviewDto()
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/worker-review")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER1, roles = [AuthZRole.WORKER])
    fun review_task_worker_task() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID, reviewerUuid = UUID.fromString(WORKER1))
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/worker-review")
            .then()
                .statusCode(200)
        //@formatter:on
    }


    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun review_task_user_no_cookie() {
        val reviewDto = TestDataUtils.getReviewDto(reviewerType = ReviewerType.USER)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/user-review")
            .then()
                .statusCode(401)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun review_task_user_from_worker() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID, reviewerType = ReviewerType.WORKER)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .body(reviewDto)
                .cookie(TestDataUtils.tokenCookie())
            .`when`()
                .post("$BASE_URL/user-review")
            .then()
                .statusCode(403)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER1, roles = [AuthZRole.USER])
    fun review_task_user_not_owner() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID, reviewerType = ReviewerType.USER)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/user-review")
            .then()
                .statusCode(400)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun review_task_user_task_not_exists() {
        val reviewDto = TestDataUtils.getReviewDto(reviewerType = ReviewerType.USER)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/user-review")
            .then()
                .statusCode(404)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER2, roles = [AuthZRole.USER])
    fun review_task_user_task() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID, reviewerUuid = UUID.fromString(USER2), reviewerType = ReviewerType.USER)
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/user-review")
            .then()
                .statusCode(200)
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER2, roles = [AuthZRole.WORKER])
    fun review_task_worker_move_task_to_done() {
        val reviewDto = TestDataUtils.getReviewDto(taskUuid = TASK_UUID2, reviewerUuid = UUID.fromString(WORKER2))
        //@formatter:off
        RestAssured
            .given()
                .accept("application/json")
                .contentType("application/json")
                .cookie(TestDataUtils.tokenCookie())
                .body(reviewDto)
            .`when`()
                .post("$BASE_URL/worker-review")
            .then()
                .statusCode(200)

        val task = RestAssured
            .given()
                .cookie(TestDataUtils.tokenCookie())
                .accept("application/json")
                .contentType("application/json")
            .`when`()
                .get("$BASE_URL/a99fb214-7c41-4a3d-a990-b499577d46c5")
            .then()
                .statusCode(200)
                .extract().body().`as`(TaskDto::class.java)

        assertEquals(TaskStatus.DONE, task.status)
        //@formatter:on
    }

}