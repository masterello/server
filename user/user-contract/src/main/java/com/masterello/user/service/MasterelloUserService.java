package com.masterello.user.service;

import com.masterello.user.value.MasterelloUser;

import java.util.Optional;
import java.util.UUID;

public interface MasterelloUserService {
    Optional<MasterelloUser> findById(UUID id);
    Optional<MasterelloUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
