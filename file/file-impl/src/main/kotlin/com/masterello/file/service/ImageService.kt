package com.masterello.file.service

import com.masterello.file.exception.FileNotProvidedException
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

@Service
class ImageService {

    fun createThumbnail(compressedImage: BufferedImage, size: Int): BufferedImage {
        return Thumbnails.of(compressedImage)
            .size(size, size)
            .keepAspectRatio(true)
            .asBufferedImage()
    }

    @Throws(IOException::class)
    fun compressedImage(originalImage: BufferedImage, compressValue: Float, fileType:String): ByteArrayOutputStream {
        val compressed = ByteArrayOutputStream()

        ImageIO.createImageOutputStream(compressed).use { outputStream ->
            val jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next()
            val jpgWriteParam = jpgWriter.defaultWriteParam

            if (fileType == "jpg") {
                jpgWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
                jpgWriteParam.compressionQuality = compressValue
                jpgWriter.output = outputStream
                jpgWriter.write(null, IIOImage(originalImage, null, null), jpgWriteParam)
                jpgWriter.dispose()
            } else {
                val newBufferedImage = BufferedImage(
                    originalImage.width,
                    originalImage.height,
                    BufferedImage.TYPE_INT_BGR
                )

                newBufferedImage.createGraphics()
                    .drawImage(newBufferedImage, 0, 0, Color.white, null)

                val g2d = newBufferedImage.createGraphics()
                g2d.color = Color.WHITE
                g2d.fillRect(0, 0, newBufferedImage.width, newBufferedImage.height)
                g2d.drawImage(originalImage, 0, 0, null)
                g2d.dispose()

                newBufferedImage.flush()

                jpgWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
                jpgWriteParam.compressionQuality = compressValue

                jpgWriter.output = outputStream
                jpgWriter.write(null, IIOImage(newBufferedImage, null, null), jpgWriteParam)
                jpgWriter.dispose()
            }
        }

        return compressed
    }

    fun createBufferedImage(file: MultipartFile?): BufferedImage {
        file?.inputStream ?: throw FileNotProvidedException("File is not provided")
        return ImageIO.read(file.inputStream)
    }
}