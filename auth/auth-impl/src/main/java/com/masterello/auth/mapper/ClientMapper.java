package com.masterello.auth.mapper;

import com.masterello.auth.domain.MasterelloRegisteredClient;
import com.masterello.auth.dto.ClientDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    MasterelloRegisteredClient mapToClientEntity(ClientDTO clientDTO);

}
