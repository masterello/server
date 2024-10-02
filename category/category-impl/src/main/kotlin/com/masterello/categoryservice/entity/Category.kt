package com.masterello.categoryservice.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Generated
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.generator.EventType
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "category", schema = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID? = null,

    @Column(name = "name", length = 255)
    var name: String? = null,

    @Column(name = "original_name", length = 255)
    var originalName: String? = null,

    @Column(name = "description", length = 255)
    var description: String? = null,

    @Generated(event = [EventType.INSERT])
    @Column(name = "category_code", insertable = false, updatable = false)
    val categoryCode: Int? = null,

    @Column(name = "parent_code")
    var parentCode: Int? = null,

    @Column(name = "is_service")
    var isService: Boolean? = null,

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    val updatedDate: OffsetDateTime? = null,

    @Column(name = "active")
    var active: Boolean? = false
)
