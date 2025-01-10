package com.masterello.task.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "user_task_review", schema = "public")
data class UserTaskReview(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID? = null,

    @Column(name = "task_uuid")
    var taskUuid: UUID = UUID.randomUUID(),

    @Column(name = "user_uuid")
    var userUuid: UUID = UUID.randomUUID(),

    @Column(name = "review", length = 755)
    var review: String = "",

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    val updatedDate: OffsetDateTime? = null
)
