package com.masterello.commons.security.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.security.data.MasterelloAuthentication;
import lombok.val;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthBasedSerializerModifier extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        return beanProperties.stream()
                .map(writer -> {
                    AuthGuard annotation = writer.getAnnotation(AuthGuard.class);
                    if (annotation != null) {
                        return new RestrictedFieldWriter(writer, annotation);
                    }
                    return writer;
                })
                .collect(Collectors.toList());
    }

    private static class RestrictedFieldWriter extends BeanPropertyWriter {
        private final AuthGuard annotation;

        protected RestrictedFieldWriter(BeanPropertyWriter writer, AuthGuard annotation) {
            super(writer);
            this.annotation = annotation;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider provider) throws Exception {
            if (shouldSerialize()) {
                super.serializeAsField(bean, gen, provider);
            }
        }

        private boolean shouldSerialize() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || isAnonymous(auth)) {
                return false;
            }
            val userRoles = ((MasterelloAuthentication) auth).getDetails().getUserRoles();
            val requiredRoles = Arrays.stream(annotation.roles()).collect(Collectors.toSet());
            return hasAnyRole(userRoles, requiredRoles);
        }

        private boolean isAnonymous(Authentication auth) {
            return auth instanceof AnonymousAuthenticationToken;
        }

        public static boolean hasAnyRole(List<AuthZRole> userRoles, Set<AuthZRole> requiredRoles) {
            return requiredRoles.isEmpty() || userRoles.stream().anyMatch(requiredRoles::contains);
        }
    }
}