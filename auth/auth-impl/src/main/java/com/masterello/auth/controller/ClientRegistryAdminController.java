package com.masterello.auth.controller;

import com.masterello.auth.data.AuthZRole;
import com.masterello.auth.domain.MasterelloRegisteredClient;
import com.masterello.auth.dto.ClientDTO;
import com.masterello.auth.mapper.ClientMapper;
import com.masterello.auth.repository.MasterelloRegisteredClientRepository;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/client")
public class ClientRegistryAdminController {

    private final MasterelloRegisteredClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @PostMapping()
    public void registerClient(@RequestBody ClientDTO clientDTO) {
        MasterelloRegisteredClient client = clientMapper.mapToClientEntity(clientDTO);
        clientRepository.save(client);
    }
}
