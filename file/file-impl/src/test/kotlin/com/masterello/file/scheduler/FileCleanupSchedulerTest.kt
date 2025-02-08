package com.masterello.file.scheduler

import com.masterello.file.service.FileService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FileCleanupSchedulerTest {

    @Mock
    private lateinit var fileService: FileService

    @InjectMocks
    private lateinit var scheduler: FileCleanupScheduler

    @Test
    fun `should call cleanupNotUploadedImages`() {
        scheduler.cleanUpNotUploadedImages()

        verify(fileService).cleanupNotUploadedImages()
    }
}