package com.masterello.task.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "user_rating", schema = "public")
data class UserRating(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID? = null,

    @Column(name = "task_uuid")
    var taskUuid: UUID = UUID.randomUUID(),

    @Column(name = "rating")
    var rating: Int = 1,

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    val updatedDate: OffsetDateTime? = null
)
