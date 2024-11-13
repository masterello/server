package com.masterello.file.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FileTypeValidator::class])
annotation class SearchableImage(
    val message: String = "Invalid fileType specified. Only AVATAR and PORTFOLIO are allowed.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
