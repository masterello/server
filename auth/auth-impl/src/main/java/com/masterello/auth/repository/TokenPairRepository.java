package com.masterello.auth.repository;

import com.masterello.auth.domain.TokenPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TokenPairRepository extends JpaRepository<TokenPair, String> {


    Optional<TokenPair> findByAccessTokenValue(String accessToken);

    Optional<TokenPair> findByRefreshTokenValue(String refreshToken);

    Optional<TokenPair> findByAccessTokenValueOrRefreshTokenValue(String accessToken, String refreshToken);

    Optional<TokenPair> findByAuthorizationIdAndRevokedFalse(String authorizationId);


    @Modifying
    @Transactional
    @Query("UPDATE TokenPair t SET t.revoked = true WHERE t.authorization.id = :authorizationId")
    void revokeAllTokensByAuthorizationId(@Param("authorizationId") String authorizationId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenPair t WHERE t.authorization.id = :authorizationId")
    void deleteAllTokensByAuthorizationId(@Param("authorizationId") String authorizationId);
}
