package com.masterello.commons.core.validation.mapper;

import com.masterello.commons.core.validation.dto.ValidationErrorsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ValidationErrorMapper {

    @Mapping(target = "message", source = "defaultMessage")
    ValidationErrorsDTO.ValidationErrorDTO toValidationErrorDTO(FieldError fieldError);

    // Map BindingResult to ValidationErrorsDTO
    default ValidationErrorsDTO toValidationErrorsDTO(BindingResult bindingResult) {
        List<ValidationErrorsDTO.ValidationErrorDTO> errorDTOs = bindingResult.getFieldErrors()
                .stream()
                .map(this::toValidationErrorDTO)
                .toList(); // Java 16+; for Java 11 use collect(Collectors.toList())

        return ValidationErrorsDTO.builder()
                .errors(errorDTOs)
                .build();
    }
}