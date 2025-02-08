package com.masterello.file.scheduler

import com.masterello.file.service.FileService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FileCleanupScheduler(private val fileService: FileService) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${masterello.file.cleanup-cron}")
    fun cleanUpNotUploadedImages() {
        log.info { "Start images cleanup" }
        fileService.cleanupNotUploadedImages()
        log.info { "Finished images cleanup" }
    }
}