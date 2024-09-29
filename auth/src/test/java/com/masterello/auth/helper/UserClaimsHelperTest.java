package com.masterello.auth.helper;

import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserClaimsHelperTest {

    @Test
    void getUserClaims_ReturnsCorrectClaims() {
        // Arrange
        MasterelloTestUser user = new MasterelloTestUser();
        user.setRoles(Set.of(Role.USER, Role.ADMIN));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmail("john_doe@test.com");
        user.setUuid(UUID.randomUUID());
        user.setEmailVerified(true);

        // Act
        Map<String, Object> userClaims = UserClaimsHelper.getUserClaims(user);

        // Assert
        assertEquals(Set.of("USER", "ADMIN"), userClaims.get("roles"));
        assertEquals("ACTIVE", userClaims.get("userStatus"));
        assertEquals("john_doe@test.com", userClaims.get("username"));
        assertEquals(user.getUuid().toString(), userClaims.get("userId"));
        assertEquals(true, userClaims.get("emailVerified"));
    }

    @Test
    void getUserClaims_EmptyRoles_ReturnsCorrectClaims() {
        // Arrange
        MasterelloTestUser user = new MasterelloTestUser();
        user.setRoles(Set.of());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmail("jane_doe@test.com");
        user.setUuid(UUID.randomUUID());
        user.setEmailVerified(false);

        // Act
        Map<String, Object> userClaims = UserClaimsHelper.getUserClaims(user);

        // Assert
        assertEquals(Set.of(), userClaims.get("roles"));
        assertEquals("ACTIVE", userClaims.get("userStatus"));
        assertEquals("jane_doe@test.com", userClaims.get("username"));
        assertEquals(user.getUuid().toString(), userClaims.get("userId"));
        assertEquals(false, userClaims.get("emailVerified"));
    }
}