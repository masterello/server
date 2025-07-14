package com.masterello.worker.dto;

import com.masterello.worker.domain.WorkerTranslationLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TranslatedWorkerServiceDTO extends WorkerServiceDTO {

    private Map<WorkerTranslationLanguage, TextTranslationDTO> translatedDetails;
}
