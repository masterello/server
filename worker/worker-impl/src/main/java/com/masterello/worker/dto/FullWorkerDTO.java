package com.masterello.worker.dto;

import com.masterello.user.value.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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
    private String city;
    private List<Language> languages;
    private WorkerInfoDTO workerInfo;
}
