package com.masterello.auth.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class SerializablePrincipal {

    private String registeredClientId;
    private String clientAuthenticationMethod;
    private String userId;
    private boolean authenticated;
}
