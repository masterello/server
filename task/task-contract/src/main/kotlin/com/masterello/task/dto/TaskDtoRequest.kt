package com.masterello.task.dto

import jakarta.validation.constraints.Min

data class TaskDtoRequest(
    @field:Min(0) val page: Int = 0,
    @field:Min(1) val pageSize: Int = 10,
    val sort: Sort = DEFAULT_SORT,
    val categoryCodes: List<@Min(1) Int> = emptyList()
) {
    data class Sort(
        val order: SortOrder = SortOrder.ASC,
        val fields: List<String> = listOf("id")
    )

    enum class SortOrder {
        ASC,
        DESC
    }

    companion object {
        val DEFAULT_SORT: Sort = Sort(
            order = SortOrder.ASC,
            fields = listOf("createdDate")
        )
    }
}