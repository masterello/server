package com.masterello.task.dto

enum class TaskStatus(val code: Int) {
    NEW(0),
    ASSIGNED_TO_WORKER(1),
    IN_REVIEW(2),
    CANCELLED(3),
    DONE(4);
}