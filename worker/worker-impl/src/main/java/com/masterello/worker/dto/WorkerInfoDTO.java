package com.masterello.worker.dto;

import com.masterello.commons.security.serialization.AuthGuard;
import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import com.masterello.worker.domain.Language;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerInfoDTO {

    private String description;
    @AuthGuard
    private String phone;
    @AuthGuard
    private String telegram;
    @AuthGuard
    private String whatsapp;
    @AuthGuard
    private String viber;
    @NotNull
    private Country country;
    @NotNull
    private City city;
    private List<WorkerServiceDTO> services;
    private List<Language> languages;
    private Instant registeredAt;
}
