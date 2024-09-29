package com.masterello.user.repository;

import com.masterello.user.domain.MasterelloUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<MasterelloUserEntity, UUID> {

    Optional<MasterelloUserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
