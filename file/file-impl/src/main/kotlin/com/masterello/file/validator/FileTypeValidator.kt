package com.masterello.file.validator

import com.masterello.file.dto.FileType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class FileTypeValidator : ConstraintValidator<SearchableImage, FileType> {
    override fun isValid(value: FileType?, context: ConstraintValidatorContext?): Boolean {
        return value == FileType.AVATAR || value == FileType.PORTFOLIO
    }
}