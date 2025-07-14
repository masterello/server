package com.masterello.worker.domain;

import com.masterello.translation.aspect.Translated;
import com.masterello.translation.aspect.TranslationKey;
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

    @TranslationKey
    @Column(name = "service_id")
    private Integer serviceId;
    private Integer amount;
    @Translated(event = ServiceDetailsChangedEvent.class)
    private String details;
}

