package com.masterello.worker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TranslatedWorkerServiceProjection extends WorkerServiceEntity {

    private Map<WorkerTranslationLanguage, WorkerServiceDetailsEntity> translatedDetails;

}
