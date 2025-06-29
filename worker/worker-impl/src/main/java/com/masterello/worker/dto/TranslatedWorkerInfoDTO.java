package com.masterello.worker.dto;

import com.masterello.worker.domain.TranslationLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TranslatedWorkerInfoDTO extends WorkerInfoDTO {

    private Map<TranslationLanguage, WorkerDescriptionDTO> descriptions;
}
