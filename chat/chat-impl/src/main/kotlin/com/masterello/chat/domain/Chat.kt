package com.masterello.chat.domain;

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name = "chat")
data class Chat(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: UUID? = null,

        @Column(name = "task_id")
        val taskId: UUID? = null,

        @Column(name = "user_id")
        val userId: UUID? = null,

        @Column(name = "worker_id")
        val workerId: UUID? = null
)