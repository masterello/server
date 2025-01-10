package com.masterello.task.dto

import jakarta.validation.constraints.Min

data class TaskDtoRequest(
    @Min(1) val page: Int = 0,
    @Min(1) val pageSize: Int = 10,
    val sort: Sort = DEFAULT_SORT
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