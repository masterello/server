package com.masterello.auth.repository;

import com.masterello.auth.domain.AuthorizationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuthorizationRequestEntityRepository extends JpaRepository<AuthorizationRequestEntity, String> {

    void deleteAllByExpiresAtBefore(Instant time);
}
