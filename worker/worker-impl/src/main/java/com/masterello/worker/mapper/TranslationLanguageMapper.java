package com.masterello.worker.mapper;

import com.masterello.translation.dto.TranslationLanguage;
import com.masterello.worker.domain.WorkerTranslationLanguage;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper(componentModel = "spring")
public interface TranslationLanguageMapper {

    @ValueMappings({
            @ValueMapping(source = "DE", target = "DE"),
            @ValueMapping(source = "EN", target = "EN"),
            @ValueMapping(source = "UK", target = "UK"),
            @ValueMapping(source = "RU", target = "RU"),
            @ValueMapping(source = "OTHER", target = "OTHER"),
    })
    WorkerTranslationLanguage translationLanguageToWorkerLanguage(TranslationLanguage translationLanguage);

}
