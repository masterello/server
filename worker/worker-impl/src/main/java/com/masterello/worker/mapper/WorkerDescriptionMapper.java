package com.masterello.worker.mapper;

import com.masterello.worker.domain.WorkerDescriptionEntity;
import com.masterello.worker.dto.WorkerDescriptionDTO;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier("workerInfoMapper")
@Mapper(componentModel = "spring",
        uses = WorkerServiceMapper.class)
public interface WorkerDescriptionMapper {

    WorkerDescriptionDTO mapToDto(WorkerDescriptionEntity workerDescriptionEntity);
}
