package com.masterello.user.repository;

import com.masterello.user.domain.ConfirmationLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmationLinkRepository extends JpaRepository<ConfirmationLink, UUID> {

    Optional<ConfirmationLink> findByToken(String token);

    @Query(nativeQuery = true, value =
            """
            SELECT count(*) from confirmation_link cl
            where cl.user_uuid = :userUuid and cl.creation_date > NOW() - INTERVAL '1 DAY'
            """)
    Integer findConfirmationCountsByUserUuid(@Param("userUuid") UUID userUuid);
}
