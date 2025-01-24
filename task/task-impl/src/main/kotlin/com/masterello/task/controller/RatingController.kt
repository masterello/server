package com.masterello.task.controller

import com.masterello.task.service.RatingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Validated
@RestController
@RequestMapping("/api/rating")
@Tag(name = "Rating", description = "API for managing ratings")
class RatingController {
    @Autowired
    private lateinit var ratingService: RatingService

    /**
     * Retrieve user rating
     * @param userUuid the identifier of the user
     * @return User rating or nothing if there is no rates
     */
    @Operation(summary = "Retrieve user rating", description = "Retrieve user rating")
    @ApiResponse(responseCode = "200", description = "Retrieve user rating")
    @GetMapping("/user/{userUuid}")
    fun getUserRating(@PathVariable userUuid: UUID): ResponseEntity<String> {
        return ResponseEntity.ok(ratingService.calculateUserRating(userUuid))
    }

    /**
     * Retrieve worker rating
     * @param workerUuid the identifier of the worker
     * @return Worker rating or nothing if there is no rates
     */
    @Operation(summary = "Retrieve worker rating", description = "Retrieve worker rating")
    @ApiResponse(responseCode = "200", description = "Retrieve worker rating")
    @GetMapping("/worker/{workerUuid}")
    fun getWorkerRating(@PathVariable workerUuid: UUID): ResponseEntity<String> {
        return ResponseEntity.ok(ratingService.calculateWorkerRating(workerUuid))
    }
}