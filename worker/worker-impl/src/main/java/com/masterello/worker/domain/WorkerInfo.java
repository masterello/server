package com.masterello.worker.domain;

import com.masterello.commons.core.json.Patchable;
import com.masterello.commons.core.sort.Sortable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "worker_info", schema = "public")
public class WorkerInfo {

    @Id
    @Column(name = "worker_id")
    private UUID workerId;

    @Patchable
    @Sortable
    @Column(name = "description")
    private String description;

    @Patchable
    @Column(name = "phone")
    private String phone;

    @Patchable
    @Column(name = "telegram")
    private String telegram;

    @Patchable
    @Column(name = "whatsapp")
    private String whatsapp;

    @Patchable
    @Column(name = "viber")
    private String viber;

    @Patchable
    @ElementCollection(targetClass = WorkerServiceEntity.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "worker_services", joinColumns = @JoinColumn(name = "worker_id"))
    private List<WorkerServiceEntity> services;
}
