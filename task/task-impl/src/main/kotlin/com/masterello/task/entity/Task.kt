package com.masterello.task.entity

import com.masterello.task.converter.TaskStatusConverter
import com.masterello.task.dto.TaskStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "task", schema = "public")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID? = null,

    @Column(name = "user_uuid")
    var userUuid: UUID = UUID.randomUUID(),

    @Column(name = "worker_uuid")
    var workerUuid: UUID? = null,

    @Column(name = "name", length = 255)
    var name: String = "",

    @Column(name = "description", length = 755)
    var description: String = "",

    @Column(name = "category_code")
    var categoryCode: Int = 0,

    @Convert(converter = TaskStatusConverter::class)
    @Column(name = "status")
    var status: TaskStatus = TaskStatus.NEW,

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    val updatedDate: OffsetDateTime? = null
)
