package com.masterello.category.dto

import java.time.OffsetDateTime
import java.util.*

data class CategoryDto (
        val uuid: UUID? = null,
        val name: String? = null,
        val description: String? = null,
        val categoryCode: Int? = null,
        val parentCode: Int? = null,
        val isService: Boolean? = null,
        val createdDate: OffsetDateTime? = null,
        val updatedDate: OffsetDateTime? = null,
        val active: Boolean? = null
)
