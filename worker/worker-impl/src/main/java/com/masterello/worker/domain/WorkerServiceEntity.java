package com.masterello.worker.domain;

import com.masterello.commons.data.change.aspect.OnChange;
import com.masterello.commons.data.change.aspect.OnChangeKey;
import com.masterello.worker.event.ServiceDetailsChangedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class WorkerServiceEntity {

    @OnChangeKey
    @Column(name = "service_id")
    private Integer serviceId;
    private Integer amount;
    @OnChange(event = ServiceDetailsChangedEvent.class)
    private String details;
}

