package com.masterello.worker.mapper;

import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.dto.FullWorkerDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = WorkerInfoMapper.class)
public interface FullWorkerMapper {
    FullWorkerProjection mapToEntity(FullWorkerDTO fullWorkerDTO);

    FullWorkerDTO mapToDto(FullWorkerProjection fullWorkerProjection);
}
