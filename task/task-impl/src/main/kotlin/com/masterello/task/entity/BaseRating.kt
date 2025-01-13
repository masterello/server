package com.masterello.task.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.*

@MappedSuperclass
abstract class BaseRating(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val uuid: UUID? = null,

    @Column(name = "task_uuid", nullable = false)
    open var taskUuid: UUID = UUID.randomUUID(),

    @Column(name = "rating", nullable = false)
    open var rating: Int = 1,

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    open val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    open val updatedDate: OffsetDateTime? = null
)