package com.masterello.auth.repository;

import com.masterello.auth.domain.MasterelloRegisteredClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterelloRegisteredClientRepository extends JpaRepository<MasterelloRegisteredClient, String> {

    Optional<MasterelloRegisteredClient> findByClientId(String clientId);
}
