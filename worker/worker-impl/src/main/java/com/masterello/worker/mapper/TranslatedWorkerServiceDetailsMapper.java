package com.masterello.worker.mapper;

import com.masterello.worker.domain.TranslatedWorkerServiceProjection;
import com.masterello.worker.domain.WorkerServiceDetailsEntity;
import com.masterello.worker.domain.WorkerServiceEntity;
import com.masterello.worker.domain.WorkerTranslationLanguage;
import com.masterello.worker.dto.TranslatedWorkerServiceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface TranslatedWorkerServiceDetailsMapper {
    @Mapping(target = "translatedDetails", source = "translatedServicesEntitySet")
    TranslatedWorkerServiceProjection mapToTranslatedWorkerInfo(WorkerServiceEntity workerService, Map<WorkerTranslationLanguage, WorkerServiceDetailsEntity> translatedServicesEntitySet);

    TranslatedWorkerServiceDTO mapToDTO(TranslatedWorkerServiceProjection projection);
}
