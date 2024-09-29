package com.masterello.user.repository;

import com.masterello.user.domain.ConfirmationLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmationLinkRepository extends JpaRepository<ConfirmationLink, UUID> {

    Optional<ConfirmationLink> findByUserUuid(UUID uuid);

    Optional<ConfirmationLink> findByToken(String token);
}
