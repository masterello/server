package com.masterello.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullWorkerDTO {

    private UUID uuid;
    private String title;
    private String name;
    private String lastname;
    private TranslatedWorkerInfoDTO workerInfo;
}
