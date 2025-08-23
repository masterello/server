package com.masterello.user.repository;

import com.masterello.user.domain.ConfirmationLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmationLinkRepository extends JpaRepository<ConfirmationLink, UUID> {

    Optional<ConfirmationLink> findByTokenAndUserUuid(String token, UUID userId);

    @Query(nativeQuery = true, value =
            """
            SELECT count(*) from confirmation_link cl
            where cl.user_uuid = :userUuid and cl.creation_date > NOW() - INTERVAL '1 DAY'
            """)
    Integer findConfirmationCountsByUserUuid(@Param("userUuid") UUID userUuid);

    @Modifying
    @Transactional
    @Query("UPDATE ConfirmationLink cl SET cl.token = NULL " +
            "WHERE cl.creationDate < :expirationThreshold AND cl.token IS NOT NULL")
    int nullOutExpiredCodes(@Param("expirationThreshold") OffsetDateTime expirationThreshold);
}
