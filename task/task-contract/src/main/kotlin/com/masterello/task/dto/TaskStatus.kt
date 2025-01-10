package com.masterello.task.dto

enum class TaskStatus(val code: Int) {
    NEW(0),
    ASSIGNED_TO_WORKER(1),
    IN_PROGRESS(2),
    IN_REVIEW(3),
    CANCELLED(4),
    DONE(5);
}