package com.masterello.file.util

import com.masterello.file.dto.FileDto
import com.masterello.file.exception.FileNameException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.net.FileNameMap
import java.net.URLConnection

object FileUtil {

    fun prepareHeaders(filename: String): HttpHeaders {
        val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
        val contentType = fileNameMap.getContentTypeFor(filename) ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
        return HttpHeaders().apply {
            add("Content-Type", MediaType.valueOf(contentType).toString())
            add("Content-Disposition", "inline; filename=$filename")
        }
    }

    fun getFileNameWithoutExtension(filename: String): String {
        return filename.substringBeforeLast(".")
    }

    fun getFileExtension(filename: String): String {
        return filename.substringAfterLast(".", "")
    }

    fun getFileName(payload: FileDto): String {
        return payload.fileName?.takeIf { it.isNotBlank() }
            ?: payload.file?.originalFilename?.takeIf { it.isNotBlank() }
            ?: throw FileNameException("File name is not provided")
    }
}