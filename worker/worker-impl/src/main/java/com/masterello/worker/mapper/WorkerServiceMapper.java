package com.masterello.worker.mapper;

import com.masterello.worker.domain.WorkerServiceEntity;
import com.masterello.worker.dto.WorkerServiceDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkerServiceMapper {
    WorkerServiceEntity mapToEntity(WorkerServiceDTO workerServiceDTO);

    WorkerServiceDTO mapToDto(WorkerServiceEntity workerService);
}
