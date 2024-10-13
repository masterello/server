package com.masterello.file.controller

import com.masterello.file.dto.FileDto
import com.masterello.file.service.FileService
import com.masterello.file.util.FileUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "API for managing user images and documents")
class FileController {

    @Autowired
    private lateinit var fileService: FileService

    @GetMapping("/{userUuid}")
    @Operation(summary = "Get all files by user UUID", description = "Retrieve all files for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved files")
    fun getAllFilesByUserUuid(@PathVariable userUuid: UUID): ResponseEntity<List<FileDto>> {
        val files = fileService.findAllFilesByUserUuid(userUuid)
        return ResponseEntity.ok(files)
    }

    @Operation(summary = "Upload user file", description = "Upload user file")
    @ApiResponse(responseCode = "200", description = "Creates user file and uploads it to the server")
    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(@Valid @ModelAttribute("payload") payload: FileDto
    ): ResponseEntity<String> {
        fileService.storeFile(payload)
        return ResponseEntity.ok("created file")
    }

    @Operation(summary = "Delete user file", description = "Delete user file")
    @ApiResponse(responseCode = "204", description = "Deletes user file and removes it from the server")
    @DeleteMapping("/{userUuid}/{fileUuid}")
    fun removeFile(@PathVariable userUuid: UUID, @PathVariable fileUuid: UUID): ResponseEntity<Void> {
        val result = fileService.removeFile(userUuid, fileUuid)
        return if (result) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }


    @GetMapping("/{userUuid}/{fileUuid}")
    @Operation(summary = "Get all files by user UUID", description = "Retrieve all files for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved files")
    fun downloadFile(@PathVariable userUuid: UUID, @PathVariable fileUuid: UUID) : ResponseEntity<ByteArray> {
        val fileData = fileService.downloadUserFile(userUuid, fileUuid)

        return if (fileData != null) {
            val (fileName, file) = fileData
            val headers: HttpHeaders = FileUtil.prepareHeaders(fileName)
            ResponseEntity.ok()
                .headers(headers)
                .body(file)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}