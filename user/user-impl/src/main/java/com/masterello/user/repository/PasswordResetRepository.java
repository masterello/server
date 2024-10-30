package com.masterello.user.repository;

import com.masterello.user.domain.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {

    Optional<PasswordReset> findByToken(String token);

    @Query(nativeQuery = true, value =
            """
            SELECT count(*) from password_reset pr
            where pr.user_uuid = :userUuid and pr.creation_date > NOW() - INTERVAL '1 DAY'
            """)
    Integer findResetCountsByUserUuid(@Param("userUuid") UUID userUuid);

    @Modifying
    void deleteAllByUserUuid(UUID userUuid);
}

