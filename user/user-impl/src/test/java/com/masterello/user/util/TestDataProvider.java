package com.masterello.user.util;

import com.masterello.user.domain.ConfirmationLink;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.domain.PasswordReset;
import com.masterello.user.domain.SupportRequest;
import com.masterello.user.dto.SupportRequestDTO;
import com.masterello.user.dto.UserDTO;
import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import io.restassured.http.Cookie;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;


public final class TestDataProvider {

    public static final String FROM = "masterello1234@gmail.com";
    public static final String SENDER = "Masterello";
    public static final String SUBJECT = "Please verify your registration";
    public static final String RESET_SUBJECT = "Reset password";
    public static final String LOCALE = "en";

    public static final String CLIENT_BEARER = "Basic Z3c6M1RaLCZdL0VyQHRicCZQQmRofDtScHNvY2tQdygo";

    public static final String VERIFIED_USER_S ="49200ea0-3879-11ee-be56-0242ac120002";

    public static final UUID VERIFIED_USER = UUID.fromString(VERIFIED_USER_S);

    public static final String VERIFIED_USER_2_S = "ba7bb05a-80b3-41be-8182-66608aba2a31";
    public static final UUID VERIFIED_USER_2 = UUID.fromString(VERIFIED_USER_2_S);
    public static final String VERIFIED_USER_EMAIL = "verified@gmail.com";
    public static final String VERIFIED_USER_PASS = "password";
    public static final UUID NOT_VERIFIED_LINK_EXPIRED_USER = UUID.fromString("e8b0639f-148c-4f74-b834-bbe04072a999");
    public static final String NOT_VERIFIED_LINK_EXPIRED_USER_EMAIL = "not_verified_link_expired@gmail.com";
    public static final UUID NOT_VERIFIED_LINK_VALID_USER = UUID.fromString("e8b0639f-148c-4f74-b834-bbe04072a416");
    public static final String NOT_VERIFIED_LINK_VALID_USER_EMAIL = "not_verified_link_valid@gmail.com";
    public static final String ACCESS_TOKEN = "eodTcZFDW4x2P95ZgiXfGK19dbz6FNgHMpOUNLF0Q9ca2GRyi7Nt-5le_tsbHFP7EA6zcCsKIxgERswmo_cWwTnz3c6WxtTSWG3PZ0SX-K7JJ00HiMx4SBu2ESo4LcZH";

    public static MasterelloUserEntity buildUser() {
        return MasterelloUserEntity.builder()
                .country(Country.GERMANY)
                .city(City.BERLIN)
                .email("test@masterello.com")
                .name("Name")
                .lastname("Surname")
                .phone("12345677")
                .password("pass")
                .emailVerified(false)
                .build();
    }

    public static MasterelloUserEntity buildCompleteUser() {
        var user = buildUser();
        user.setUuid(UUID.randomUUID());
        user.setEmailVerified(false);

        return user;
    }

    public static UserDTO buildUserDto() {
        return UserDTO.builder()
                .country(Country.GERMANY)
                .city(City.BERLIN)
                .email("test@masterello.com")
                .name("Name")
                .lastname("Surname")
                .phone("12345677")
                .uuid(UUID.randomUUID())
                .emailVerified(false)
                .build();
    }

    public static ConfirmationLink buildConfirmationLink() {
        return ConfirmationLink.builder()
                .uuid(UUID.randomUUID())
                .userUuid(UUID.randomUUID())
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .token(UUID.randomUUID().toString())
                .build();
    }

    public static Cookie tokenCookie() {
        return new Cookie.Builder(M_TOKEN_COOKIE, ACCESS_TOKEN).build();
    }

    public static SupportRequest buildSupportRequest() {
        return SupportRequest.builder()
                .title("Support 1")
                .email("test@test.com")
                .phone("91231")
                .message("Login is not working")
                .processed(false)
                .build();
    }

    public static SupportRequestDTO buildSupportRequestDto() {

        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setTitle("Support 1");
        dto.setEmail("test@test.com");
        dto.setPhone("91231");
        dto.setMessage("Login is not working");
        return dto;
    }

    public static PasswordReset buildPasswordResetEntity() {
        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUuid(UUID.randomUUID());
        passwordReset.setToken("test");
        passwordReset.setCreationDate(OffsetDateTime.now());
        passwordReset.setUserUuid(VERIFIED_USER);
        passwordReset.setExpiresAt(OffsetDateTime.now().plusHours(1));
        return passwordReset;
    }
}
