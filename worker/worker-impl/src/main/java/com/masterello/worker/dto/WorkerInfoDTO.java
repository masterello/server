package com.masterello.worker.dto;

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
    private List<WorkerServiceDTO> services;
}
