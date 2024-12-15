package com.masterello.worker.dto;

import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerInfoDTO {

    private String description;
    private String phone;
    private String telegram;
    private String whatsapp;
    private String viber;
    @NotNull
    private Country country;
    @NotNull
    private City city;
    private List<WorkerServiceDTO> services;
}
