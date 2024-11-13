package com.masterello.file.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

class ImageServiceTest {
    private val imageService = ImageService()

    @Test
    fun `test createThumbnail`() {
        val originalImage = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
        val size = 100

        val thumbnail = imageService.createThumbnail(originalImage, size)

        assertNotNull(thumbnail)
        assertEquals(size, thumbnail.width)
        assertEquals(size, thumbnail.height)
    }

    @Test
    fun `test compressedImage with non-jpg format`() {
        val originalImage = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
        val compressValue = 0.5f
        val fileType = "png"

        val compressed = ByteArrayOutputStream()
        assertDoesNotThrow {
            val output = imageService.compressedImage(originalImage, compressValue, fileType)
            compressed.write(output.toByteArray())
        }

        assertTrue(compressed.size() > 0)
    }
}