package com.masterello.file.controller

import com.masterello.auth.data.AuthZRole
import com.masterello.commons.security.validation.AuthZRule
import com.masterello.commons.security.validation.AuthZRules
import com.masterello.commons.security.validation.OwnerId
import com.masterello.file.dto.BulkImageResponseDto
import com.masterello.file.dto.BulkImageSearchRequest
import com.masterello.file.dto.FileDto
import com.masterello.file.service.FileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "API for managing user images and documents")
class FileController(
        private val fileService: FileService
) {
    @GetMapping("/{userUuid}/images")
    @Operation(summary = "Get all files by user UUID", description = "Retrieve all files for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved images")
    fun getAllImagesByUserUuid(@PathVariable userUuid: UUID): ResponseEntity<List<FileDto>> {
        val files = fileService.findAllImagesByUserUuid(userUuid)
        return ResponseEntity.ok(files)
    }

    @PostMapping("/imageSearchBulk")
    @Operation(summary = "Get images specified by type and userUuids",
        description = "Retrieve all images specified by type and userUuids")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved images specified by type and userUuids")
    fun getImagesByTypeBulk(@Valid @RequestBody bulkImageSearchRequest: BulkImageSearchRequest):
            ResponseEntity<List<BulkImageResponseDto>> {
        val response = fileService.findImagesBulk(bulkImageSearchRequest.fileType, bulkImageSearchRequest.userUuids)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{userUuid}")
    @Operation(summary = "Get all files by user UUID", description = "Retrieve all files for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved files")
    fun getAllFilesByUserUuid(@PathVariable userUuid: UUID): ResponseEntity<List<FileDto>> {
        val files = fileService.findAllFilesByUserUuid(userUuid)
        return ResponseEntity.ok(files)
    }

    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER, AuthZRole.WORKER]),
        AuthZRule(roles = [AuthZRole.ADMIN])
    )
    @Operation(summary = "Upload user file", description = "Upload user file")
    @ApiResponse(responseCode = "200", description = "Creates user file")
    @PostMapping("/upload")
    fun uploadFile(@Valid @RequestBody payloads: List<FileDto>
    ): ResponseEntity<List<FileDto>> {
        val response = fileService.storeFile(payloads)
        return ResponseEntity.ok(response)
    }

    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER, AuthZRole.WORKER]),
        AuthZRule(roles = [AuthZRole.ADMIN])
    )
    @Operation(summary = "Marks user files as uploaded", description = "Marks user files as uploaded")
    @ApiResponse(responseCode = "200", description = "Marks user files as uploaded")
    @PostMapping("/{userUuid}/confirm")
    fun confirmFileUploading(@PathVariable userUuid: UUID,
                             @Valid @RequestBody files: List<UUID>
    ): ResponseEntity<List<FileDto>> {
        val response = fileService.markAsUploaded(userUuid, files)
        return ResponseEntity.ok(response)
    }

    @AuthZRules(
        AuthZRule(roles = [AuthZRole.USER, AuthZRole.WORKER], isOwner = true),
        AuthZRule(roles = [AuthZRole.ADMIN])
    )
    @Operation(summary = "Delete user file", description = "Delete user file")
    @ApiResponse(responseCode = "204", description = "Deletes user file and removes it from the server")
    @DeleteMapping("/{userUuid}/{fileUuid}")
    fun removeFile(@OwnerId @PathVariable userUuid: UUID, @PathVariable fileUuid: UUID): ResponseEntity<Void> {
        val result = fileService.removeFile(userUuid, fileUuid)
        return if (result) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}