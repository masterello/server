package com.masterello.worker.mapper;

import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.dto.TextTranslationDTO;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier("workerInfoMapper")
@Mapper(componentModel = "spring",
        uses = WorkerServiceMapper.class)
public interface WorkerDescriptionMapper {

    TextTranslationDTO mapToDto(WorkerDescriptionEntity workerDescriptionEntity);
}
