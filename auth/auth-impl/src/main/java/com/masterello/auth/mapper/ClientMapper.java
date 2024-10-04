package com.masterello.auth.mapper;

import com.masterello.auth.domain.MasterelloRegisteredClient;
import com.masterello.auth.domain.SerializablePrincipal;
import com.masterello.auth.dto.ClientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    MasterelloRegisteredClient mapToClientEntity(ClientDTO clientDTO);

}
