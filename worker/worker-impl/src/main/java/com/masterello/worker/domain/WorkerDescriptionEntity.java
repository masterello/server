package com.masterello.worker.domain;

import com.masterello.commons.core.sort.Sortable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WorkerDescriptionEntity.WorkerDescriptionId.class)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "worker_description", schema = "public")
public class WorkerDescriptionEntity {

    @Id
    @Sortable
    @Column(name = "worker_id")
    private UUID workerId;

    @Id
    @Column(name = "language_code")
    @Enumerated(EnumType.STRING)
    private WorkerTranslationLanguage language;

    @Column(nullable = false)
    private String text;

    @Column(name = "is_original")
    private boolean original;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerDescriptionId implements Serializable {
        private UUID workerId;
        private WorkerTranslationLanguage language;
    }
}

