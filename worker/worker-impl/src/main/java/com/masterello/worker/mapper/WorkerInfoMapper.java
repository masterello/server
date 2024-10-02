package com.masterello.worker.mapper;

import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.dto.WorkerInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier("workerInfoMapper")
@Mapper(componentModel = "spring",
        uses = WorkerServiceMapper.class)
public interface WorkerInfoMapper {

    @Mapping(target = "workerId", ignore = true)
    WorkerInfo mapToEntity(WorkerInfoDTO workerInfoDTO);

    WorkerInfoDTO mapToDto(WorkerInfo workerInfo);
}
