package com.masterello.chat

import com.masterello.auth.config.AuthConstants
import com.masterello.task.dto.TaskDto
import com.masterello.user.value.City
import com.masterello.user.value.Country
import com.masterello.user.value.MasterelloTestUser
import com.masterello.user.value.Role
import com.masterello.worker.value.Worker
import io.restassured.http.Cookie
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

class ChatTestDataProvider {
    companion object {
        const val USER_S = "bb2c6e16-2228-4ac1-8482-1f3548672b43"
        val USER = UUID.fromString(USER_S)

        const val WORKER_S = "e5fcf8dd-b6be-4a36-a85a-e2d952cc6254"
        val WORKER = UUID.fromString(WORKER_S)

        const val WORKER_WITH_CHAT_S = "57bc029c-d8e3-458f-b25a-7f73283cec98"
        val WORKER_WITH_CHAT = UUID.fromString(WORKER_WITH_CHAT_S)

        val TASK_ID = UUID.fromString("d1c822c9-0ee4-462a-a88e-7c45e3bb0e54")

        const val ACCESS_TOKEN = "bg6yX_eErXRKdklESRPHpyA5SDxzIi4EuYacVX29MKCMDcm_GniWXltRhjjh6FBbpfePaDGmVE5p72cA9agNd5WveHEK4gbm9u9tA9UqntlPLMYtFFaB"

        val CHAT = UUID.fromString("e5fcf8dd-b6be-4a36-a85a-e2d952cc6254")

        val task = TaskDto(
                uuid = TASK_ID,
                userUuid = USER,
                categoryCode = 123,
                name = "Walk my Dog",
                description = "My dog needs to be walked",
                createdDate = OffsetDateTime.now(),
                updatedDate = OffsetDateTime.now()
        )

        fun buildUser(id: UUID, name: String, lastName: String) =
                MasterelloTestUser.builder()
                        .uuid(id)
                        .name(name)
                        .lastname(lastName)
                        .build()

        fun buildWorker(uuid: UUID): Worker {
            return object : Worker {
                override fun getWorkerId(): UUID = uuid

                override fun getDescription(): String? = null

                override fun getPhone(): String? = null

                override fun getTelegram(): String? = null

                override fun getWhatsapp(): String? = null

                override fun getViber(): String? = null

                override fun isActive(): Boolean = true

                override fun getRegisteredAt(): Instant = Instant.now()

            }
        }

        fun tokenCookie(): Cookie {
            return Cookie.Builder(AuthConstants.M_TOKEN_COOKIE, ACCESS_TOKEN).build()
        }
    }
}