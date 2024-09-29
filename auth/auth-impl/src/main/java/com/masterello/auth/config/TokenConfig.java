package com.masterello.auth.config;

import com.masterello.auth.helper.UserClaimsHelper;
import com.masterello.user.value.MasterelloUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.util.Map;

@Configuration
public class TokenConfig {

    @Autowired
    private TokenProperties tokenProperties;

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                // While changing token format make sure jwtGenerator has appropriate token customizer
                .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                .accessTokenTimeToLive(tokenProperties.getAccessTokenTtl())
                .refreshTokenTimeToLive(tokenProperties.getRefreshTokenTtl())
                .build();
    }

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer());
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer() {
        return context -> {
            OAuth2ClientAuthenticationToken principal = context.getPrincipal();
            MasterelloUser user = (MasterelloUser) principal.getDetails();
            if (context.getTokenType().getValue().equals("access_token")) {

                Map<String, Object> userClaims = UserClaimsHelper.getUserClaims(user);
                context.getClaims().claims(claims -> claims.putAll(userClaims));
            }
        };
    }
}
