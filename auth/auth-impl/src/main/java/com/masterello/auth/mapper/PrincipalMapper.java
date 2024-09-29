package com.masterello.auth.mapper;

import com.masterello.auth.domain.SerializablePrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

@Mapper(componentModel = "spring")
public interface PrincipalMapper {

    @Mapping(target = "registeredClientId", source = "registeredClient.id")
    @Mapping(target = "clientAuthenticationMethod", source = "clientAuthenticationMethod.value")
    @Mapping(target = "userId", expression = "java(((com.masterello.user.value.MasterelloUser)token.getDetails()).getUuid().toString())")
    @Mapping(target = "authenticated", source = "authenticated")
    SerializablePrincipal mapToSerializablePrincipal(OAuth2ClientAuthenticationToken token);

}
