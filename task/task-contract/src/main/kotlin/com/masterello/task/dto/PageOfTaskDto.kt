package com.masterello.task.dto

data class PageOfTaskDto(
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val tasks: List<TaskDto>
)