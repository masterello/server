package com.masterello.worker.domain;

import com.masterello.commons.core.json.Patchable;
import com.masterello.commons.core.sort.Sortable;
import com.masterello.user.domain.CityConverter;
import com.masterello.user.domain.CountryConverter;
import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "worker_info", schema = "public")
public class WorkerInfo {

    @Id
    @Sortable
    @Column(name = "worker_id")
    private UUID workerId;

    @Patchable
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
    @Column(name = "country")
    @Convert(converter = CountryConverter.class)
    private Country country;

    @Patchable
    @Column(name = "city")
    @Convert(converter = CityConverter.class)
    private City city;

    @Patchable
    @ElementCollection(targetClass = Language.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "worker_languages", joinColumns = @JoinColumn(name = "worker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Set<Language> languages;

    @Patchable
    @Column(name = "viber")
    private String viber;

    @Patchable
    @ElementCollection(targetClass = WorkerServiceEntity.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "worker_services", joinColumns = @JoinColumn(name = "worker_id"))
    private Set<WorkerServiceEntity> services;

    @Sortable
    @CreatedDate
    @Column(name = "registered_at", updatable = false)
    private Instant registeredAt;
}
