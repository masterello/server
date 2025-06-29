package com.masterello.worker.mapper;

import com.masterello.worker.domain.TranslatedWorkerInfoProjection;
import com.masterello.worker.domain.TranslationLanguage;
import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.TranslatedWorkerInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring",
        uses = {WorkerServiceMapper.class, WorkerDescriptionMapper.class})
public interface TranslatedWorkerInfoMapper {
    @Mapping(target = "descriptions", source = "descriptionEntitySet")
    TranslatedWorkerInfoProjection mapToTranslatedWorkerInfo(WorkerInfo workerInfo, Map<TranslationLanguage, WorkerDescriptionEntity> descriptionEntitySet);

    TranslatedWorkerInfoDTO mapToDTO(TranslatedWorkerInfoProjection projection);

}
