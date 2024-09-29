package com.masterello.validation;

import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.data.MasterelloAuthentication;
import com.masterello.exception.UnauthorisedException;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

@Aspect
@Component
public class AuthZAspect {

    @Around("@annotation(com.masterello.validation.AuthZRules)")
    public Object processAuthZRules(ProceedingJoinPoint joinPoint) throws Throwable {
        return doProcessAuthZRules(joinPoint, method -> method.getAnnotation(AuthZRules.class).value());
    }

    @Around("@annotation(com.masterello.validation.AuthZRule)")
    public Object processAuthZRule(ProceedingJoinPoint joinPoint) throws Throwable {
        return doProcessAuthZRules(joinPoint, method -> new AuthZRule[]{method.getAnnotation(AuthZRule.class)});
    }

    private Object doProcessAuthZRules(ProceedingJoinPoint joinPoint, Function<Method, AuthZRule[]> rulesProvider) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        AuthData tokenData = ((MasterelloAuthentication) securityContext.getAuthentication()).getDetails();

        UUID ownerId = findOwnerId(joinPoint, method);
        AuthContext context = new AuthContext(tokenData, ownerId);

        AuthZRule[] rules = rulesProvider.apply(method);
        boolean authorized = Arrays.stream(rules)
                .map(this::toAuthChecker)
                .anyMatch(checker -> checker.check(context));

        if (!authorized) {
            throw new UnauthorisedException(
                    "User " + tokenData.getUserId() + " is not authorized to perform " + method.getName());
        }

        return joinPoint.proceed();
    }


    private UUID findOwnerId(ProceedingJoinPoint joinPoint, Method method) {
        // Get method parameters and their annotations
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            if (parameter.isAnnotationPresent(OwnerId.class)) {
                return (UUID) arg;
            }
        }
        return null;
    }

    private AuthChecker toAuthChecker(AuthZRule authZRule) {
        return AuthChecker.builder()
                .withOwnerFilter(authZRule.isOwner())
                .withRoleFilter(authZRule.roles())
                .withEmailVerifiedFilter(authZRule.isEmailVerified())
                .build();
    }

    private record AuthChecker(Predicate<AuthContext> predicate) {

        public static AuthCheckerBuilder builder() {
            return new AuthCheckerBuilder();
        }

        public boolean check(AuthContext context) {
            return predicate.test(context);
        }

        private static class AuthCheckerBuilder {
            Predicate<AuthContext> predicate = context -> context.tokenData != null;

            AuthCheckerBuilder withOwnerFilter(boolean isOwner) {
                if (isOwner) {
                    predicate = predicate.and(context -> {
                        var requestedUserId = context.userId();
                        var callerUserId = context.tokenData.getUserId();
                        return callerUserId != null && callerUserId.equals(requestedUserId);
                    });
                }
                return this;
            }

            AuthCheckerBuilder withRoleFilter(AuthZRole[] roles) {
                if (roles != null && roles.length > 0) {
                    predicate = predicate.and(context -> {
                        var callerRoles = context.tokenData.getUserRoles() == null ? emptyList() :
                                context.tokenData.getUserRoles();
                        return CollectionUtils.containsAny(callerRoles, roles);
                    });
                }
                return this;
            }

            AuthCheckerBuilder withEmailVerifiedFilter(boolean emailVerified) {
                if (emailVerified) {
                    predicate = predicate.and(context -> Optional.ofNullable(context.tokenData.getEmailVerified())
                            .orElse(false));
                }
                return this;
            }

            AuthChecker build() {
                return new AuthChecker(predicate);
            }

        }

    }

    private record AuthContext(AuthData tokenData, @Nullable UUID userId) {
    }
}
