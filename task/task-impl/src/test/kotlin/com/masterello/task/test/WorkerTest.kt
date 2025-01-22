package com.masterello.task.test

import com.masterello.worker.value.Worker
import java.time.Instant
import java.util.*

class WorkerTest: Worker {
    override fun getWorkerId(): UUID {
        return UUID.randomUUID()
    }

    override fun getDescription(): String {
        return "mock"
    }

    override fun getPhone(): String {
        return "123"
    }

    override fun getTelegram(): String {
        return "Telegram"
    }

    override fun getWhatsapp(): String {
        return "Whatsapp"
    }

    override fun getViber(): String {
        return "viber"
    }

    override fun isActive(): Boolean {
        return true
    }

    override fun getRegisteredAt(): Instant {
        return Instant.now()
    }
}