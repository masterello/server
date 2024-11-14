package com.masterello.auth.extension;

import com.masterello.auth.data.AuthData;
import com.masterello.auth.service.AuthService;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class AuthMockExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Get the AuthMocked annotation from the test method
        AuthMocked authMocked = context.getRequiredTestMethod().getAnnotation(AuthMocked.class);

        if (authMocked != null) {

            // Get the test instance and find the `authService` field
            Object testInstance = context.getRequiredTestInstance();
            AuthService authService = getAuthServiceFromTestInstance(testInstance);

            if (authService != null) {
                // Set up the mock for validateToken using the values from the annotation
                when(authService.validateToken(Mockito.anyString()))
                        .thenReturn(Optional.of(AuthData.builder()
                                .userId(UUID.fromString(authMocked.userId()))
                                .userRoles(Arrays.asList(authMocked.roles()))
                                .emailVerified(authMocked.emailVerified())
                                .build()));
            } else {
                throw new IllegalStateException("AuthService mock not found in test instance or parent classes");
            }

        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        AuthService authService = getAuthServiceFromTestInstance(testInstance);
        if (authService != null) {
            Mockito.reset(authService);
        }
    }

    // Utility method to get `authService` from the test instance or its superclasses
    private AuthService getAuthServiceFromTestInstance(Object instance) throws IllegalAccessException {
        Class<?> currentClass = instance.getClass();
        while (currentClass != null) {
            try {
                Field authServiceField = currentClass.getDeclaredField("authService");
                authServiceField.setAccessible(true);
                return (AuthService) authServiceField.get(instance);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass(); // Move to the superclass
            }
        }
        return null;
    }
}

