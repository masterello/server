package com.masterello.commons.core.validation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ValidationErrorsDTO {

    private List<ValidationErrorDTO> errors;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationErrorDTO {
        private String field;
        private String message;
    }
}
