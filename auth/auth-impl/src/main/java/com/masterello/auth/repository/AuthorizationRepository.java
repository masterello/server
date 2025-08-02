package com.masterello.auth.repository;

import com.masterello.auth.domain.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {

    Optional<Authorization> findByAuthorizationCodeValue(String tokenValue);
}
