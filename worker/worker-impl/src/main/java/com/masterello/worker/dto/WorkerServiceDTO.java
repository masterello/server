package com.masterello.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerServiceDTO {
    private Integer serviceId;
    private Integer amount;
    private String details;
}
