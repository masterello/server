package com.masterello.worker.mapper;

import com.masterello.worker.domain.FullWorkerProjection;
import com.masterello.worker.dto.FullWorkerDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = TranslatedWorkerInfoMapper.class)
public interface FullWorkerMapper {

    FullWorkerDTO mapToDto(FullWorkerProjection fullWorkerProjection);
}
