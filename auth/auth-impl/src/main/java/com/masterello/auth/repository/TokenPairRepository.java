package com.masterello.auth.repository;

import com.masterello.auth.domain.TokenPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface TokenPairRepository extends JpaRepository<TokenPair, String> {


    Optional<TokenPair> findByAccessTokenValue(String accessToken);

    Optional<TokenPair> findByRefreshTokenValue(String refreshToken);

    Optional<TokenPair> findByAccessTokenValueOrRefreshTokenValue(String accessToken, String refreshToken);

    Optional<TokenPair> findByAuthorizationIdAndRevokedFalse(String authorizationId);

    @Query("""
        SELECT t.authorization.id
        FROM TokenPair t
        WHERE t.revoked = false
          AND t.refreshTokenExpiresAt < :now
    """)
    Page<String> findExpiredAuthorizationIds(@Param("now") OffsetDateTime now, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE TokenPair t SET t.revoked = true WHERE t.authorization.id = :authorizationId")
    void revokeAllTokensByAuthorizationId(@Param("authorizationId") String authorizationId);
}
