package com.masterello.worker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkerServiceEntity {

    @Column(name = "service_id")
    private Integer serviceId;
    private Integer amount;
    private String details;
}

