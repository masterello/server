package com.masterello.task.entity

import com.masterello.task.converter.TaskReviewerTypeConverter
import com.masterello.task.dto.ReviewerType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "task_review", schema = "public")
data class TaskReview(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID? = null,

    @Column(name = "task_uuid")
    var taskUuid: UUID = UUID.randomUUID(),

    @Column(name = "reviewer_uuid")
    var reviewerUuid: UUID = UUID.randomUUID(),

    @Convert(converter = TaskReviewerTypeConverter::class)
    @Column(name = "reviewer_type")
    var reviewerType: ReviewerType = ReviewerType.WORKER,

    @Column(name = "review", length = 755)
    var review: String = "",

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    val updatedDate: OffsetDateTime? = null
)
