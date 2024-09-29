package com.masterello.auth.utils;

import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.SerializablePrincipal;
import com.masterello.auth.helper.UserClaimsHelper;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthTestDataProvider {

    public static final String USER_1_ID = "49200ea0-3879-11ee-be56-0242ac120002";
    public static final String USER_1_EMAIL = "user1@gmail.com";
    public static final String USER_1_PASS = "password";
    public static final String USER_1_E_PASS = "$2a$10$YtXmJtc04cZPCH32VEkQEObDJqy.X1.Gx4ecuDWiCGaqSTnAKoZMS";
    public static final String USER_2_ID = "e8b0639f-148c-4f74-b834-bbe04072a416";
    public static final String USER_2_EMAIL = "user2@gmail.com";
    public static final String USER_2_E_PASS = "$2a$10$3ReKCVLKzphYkERtB1wp2Oe8Aly4chhyDZC6TVb/ATo1kLp.JjYhO";

    public static final String CLIENT_BEARER = "Basic Z3c6M1RaLCZdL0VyQHRicCZQQmRofDtScHNvY2tQdygo";

    public static final UUID AUTH_ID = UUID.fromString("49200ea0-3879-11ee-be56-0242ac120001");
    public static final UUID CLIENT_ID = UUID.fromString("1205a7c9-f696-4076-a7ff-61d4dfe2d66e");
    public static final String REDISTERED_CLIENT_ID = "gw";
    public static final String PRINCIPAL_NAME = REDISTERED_CLIENT_ID;
    public static final String GRANT_TYPE = "password";
    public static final String ACCESS_TOKEN = "bg6yX_eErXRKdklESRPHpyA5SDxzIi4EuYacVX29MKCMDcm_GniWXltRhjjh6FBbpfePaDGmVE5p72cA9agNd5WveHEK4gbm9u9tA9UqntlPLMYtFFaB-tKgrFS5LvRU";
    public static final String REFRESH_TOKEN = "Yfi7Jn2sjCLP_0deGYXJxvUferMbfpzpKMq3VeJsTz7QvI7i4RtV-wQTRQp5CwZxDpc5Ecs0m7b_hlJYxnnWn1O54Ti_dxuvpkzi2CxKwDZOt7T9KG6MTGEwiYkbTFhb";
    public static final String EMAIL = "test@gmail.com";
    public static final UUID USER_ID = UUID.fromString("9305a7c9-f696-4076-a7ff-61d4dfe2d66e");
    public static final OffsetDateTime ACCESS_TOKEN_ISSUED_AT = OffsetDateTime.parse("2024-01-14T09:15:30.123123200Z");
    public static final OffsetDateTime ACCESS_TOKEN_EXPIRES_AT = OffsetDateTime.parse("2024-01-14T10:16:30.999988800Z");
    public static final OffsetDateTime REFRESH_TOKEN_ISSUED_AT = OffsetDateTime.parse("2024-01-13T10:15:30.123123200Z");
    public static final OffsetDateTime REFRESH_TOKEN_EXPIRES_AT = OffsetDateTime.parse("2024-01-15T10:15:30.123123200Z");
    public static final String SERIALIZED_PRINCIPAL = "{\"@class\":\"com.masterello.auth.domain.SerializablePrincipal\",\"registeredClientId\":\"1205a7c9-f696-4076-a7ff-61d4dfe2d66e\",\"clientAuthenticationMethod\":\"client_secret_basic\",\"userId\":\"9305a7c9-f696-4076-a7ff-61d4dfe2d66e\",\"authenticated\":true}";
    public static final String SEIRIALIZED_ACCESS_TOKEN_METADATA = "{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"metadata.token.claims\":{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"nbf\":[\"java.time.Instant\",1705223730.123123200],\"sub\":\"gw\",\"iat\":[\"java.time.Instant\",1705223730.123123200],\"exp\":[\"java.time.Instant\",1705227390.999988800],\"aud\":[\"java.util.Collections$SingletonList\",[\"gw\"]],\"jti\":\"25f1af65-1144-4802-a7eb-7adad126663b\",\"iss\":[\"java.net.URL\",\"http://127.0.0.1:8100\"],\"username\":\"test@gmail.com\",\"roles\":[\"java.util.HashSet\",[\"ADMIN\",\"WORKER\",\"USER\"]],\"emailVerified\":false,\"userStatus\":\"BANNED\",\"userId\":\"9305a7c9-f696-4076-a7ff-61d4dfe2d66e\"},\"metadata.token.invalidated\":false}";
    public static final String SERIALIZED_REFRESH_TOKEN_METADATA = "{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"metadata.token.invalidated\":false}";
    public static final String JTI = "25f1af65-1144-4802-a7eb-7adad126663b";

    public static OAuth2ClientAuthenticationToken prepareClientAuthData() {
        RegisteredClient client = getClient();

        OAuth2ClientAuthenticationToken clientToken = mock(OAuth2ClientAuthenticationToken.class);
        when(clientToken.getRegisteredClient()).thenReturn(client);
        when(clientToken.getClientAuthenticationMethod()).thenReturn(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        when(clientToken.getName()).thenReturn(PRINCIPAL_NAME);
        when(clientToken.isAuthenticated()).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(clientToken);
        AuthorizationServerContextHolder.setContext(mock(AuthorizationServerContext.class));
        return clientToken;
    }

    public static Authorization getAuthorization() {
        return Authorization.builder()
                .id(AUTH_ID.toString())
                .principalName(PRINCIPAL_NAME)
                .registeredClientId(CLIENT_ID.toString())
                .authorizationGrantType(GRANT_TYPE)
                .principal(SERIALIZED_PRINCIPAL)
                .accessTokenType(AccessTokenType.BEARER.getValue())
                .accessTokenValue(ACCESS_TOKEN)
                .accessTokenIssuedAt(ACCESS_TOKEN_ISSUED_AT)
                .accessTokenExpiresAt(ACCESS_TOKEN_EXPIRES_AT)
                .accessTokenMetadata(SEIRIALIZED_ACCESS_TOKEN_METADATA)
                .refreshTokenValue(REFRESH_TOKEN)
                .refreshTokenIssuedAt(REFRESH_TOKEN_ISSUED_AT)
                .refreshTokenExpiresAt(REFRESH_TOKEN_EXPIRES_AT)
                .refreshTokenMetadata(SERIALIZED_REFRESH_TOKEN_METADATA)
                .build();
    }

    public static OAuth2Authorization getOAuthAuthorization(MasterelloUser user) {
        RegisteredClient client = getClient();
        OAuth2AccessToken accessToken = getAccessToken();
        Map<String, Object> accessTokenMetadata = getAccessTokenMetadata(user);
        OAuth2RefreshToken refreshToken = getRefreshToken();

        OAuth2ClientAuthenticationToken principal = getPrincipalToken(user, client);
        return OAuth2Authorization.withRegisteredClient(client)
                .id(AUTH_ID.toString())
                .principalName(PRINCIPAL_NAME)
                .authorizationGrantType(new AuthorizationGrantType(GRANT_TYPE))
                .token(accessToken, md -> md.putAll(accessTokenMetadata))
                .token(refreshToken)
                .attribute(Principal.class.getName(), principal)
                .build();
    }

    @NotNull
    public static OAuth2ClientAuthenticationToken getPrincipalToken(MasterelloUser user, RegisteredClient client) {
        OAuth2ClientAuthenticationToken principal = new OAuth2ClientAuthenticationToken(client, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, null);
        principal.setDetails(user);
        return principal;
    }

    public static SerializablePrincipal getSerializablePrincipal(String userId, String registeredClientId) {
        return SerializablePrincipal.builder()
                .userId(userId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue())
                .registeredClientId(registeredClientId)
                .authenticated(true)
                .build();
    }

    public static OAuth2RefreshToken getRefreshToken() {
        return new OAuth2RefreshToken(REFRESH_TOKEN, REFRESH_TOKEN_ISSUED_AT.toInstant(), REFRESH_TOKEN_EXPIRES_AT.toInstant());
    }

    public static OAuth2AccessToken getAccessToken() {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, ACCESS_TOKEN, ACCESS_TOKEN_ISSUED_AT.toInstant(), ACCESS_TOKEN_EXPIRES_AT.toInstant());
    }

    public static RegisteredClient getClient() {
        return RegisteredClient.withId(CLIENT_ID.toString())
                .clientId(REDISTERED_CLIENT_ID)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
    }

    @NotNull
    public static Map<String, Object> getAccessTokenMetadata(MasterelloUser user) {
        Map<String, Object> claims = new LinkedHashMap<>(Map.of(
                "aud", singletonList(REDISTERED_CLIENT_ID),
                "sub", REDISTERED_CLIENT_ID,
                "nbf", ACCESS_TOKEN_ISSUED_AT.toInstant(),
                "exp", ACCESS_TOKEN_EXPIRES_AT.toInstant(),
                "iat", ACCESS_TOKEN_ISSUED_AT.toInstant(),
                "jti", JTI,
                "iss", UrlResource.from("http://127.0.0.1:8100").getURL()
        ));
        Map<String, Object> userClaims = UserClaimsHelper.getUserClaims(user);
        claims.putAll(userClaims);
        return Map.of(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, Collections.unmodifiableMap(claims));
    }

    public static MasterelloTestUser getUser(UUID id, String email, String ePassword, boolean verified) {
        return MasterelloTestUser.builder()
                .uuid(id)
                .email(email)
                .emailVerified(verified)
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .password(ePassword)
                .build();
    }

    public static MasterelloTestUser getUser() {
        return getUser(USER_ID,EMAIL, null, true);
    }

}
