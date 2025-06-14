package com.masterello.worker.dto;

import com.masterello.commons.core.validation.ErrorCodes;
import com.masterello.commons.security.serialization.AuthGuard;
import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import com.masterello.worker.domain.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerInfoDTO {

    private String description;
    @AuthGuard
    @NotEmpty(message = ErrorCodes.PHONE_EMPTY)
    @Length(min = 6, message = ErrorCodes.PHONE_LENGTH)
    private String phone;
    @AuthGuard
    private String telegram;
    @AuthGuard
    private String whatsapp;
    @AuthGuard
    private String viber;
    @NotNull(message = ErrorCodes.COUNTRY_EMPTY)
    private Country country;
    @NotNull(message = ErrorCodes.CITY_EMPTY)
    private City city;
    @NotEmpty(message = ErrorCodes.SERVICES_EMPTY)
    @Valid
    private List<WorkerServiceDTO> services;
    @NotEmpty(message = ErrorCodes.LANGUAGES_EMPTY)
    private List<Language> languages;
    private Instant registeredAt;
}
