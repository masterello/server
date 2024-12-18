package com.masterello.user.service;

import com.masterello.user.value.MasterelloUser;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MasterelloUserService {
    Optional<MasterelloUser> findById(UUID id);
    Optional<MasterelloUser> findByEmail(String email);

    Map<UUID, MasterelloUser> findAllByIds(Set<UUID> ids);

    boolean existsByEmail(String email);
}
