package com.masterello.user.service;

import com.masterello.commons.core.data.Locale;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class SignUpService implements AuthNService{

    private final UserService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationLinkService confirmationLinkService;

    @Transactional
    public MasterelloUser selfSignup(String email, String password, Locale locale){
        MasterelloUserEntity user = MasterelloUserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .emailVerified(false)
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();
        MasterelloUser savedUser = userDetailsService.createUser(user);

        log.info("User with email {} successfully registered. Sending confirmation link to the email", email);

        confirmationLinkService.sendConfirmationLinkSafe(savedUser, locale);
        return savedUser;
    }

    @Override
    @Transactional
    public MasterelloUser googleSignup(String email, String name, String lastName) {
    MasterelloUserEntity user = MasterelloUserEntity.builder()
                .email(email)
                .emailVerified(true)
                .name(name)
                .lastname(lastName)
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();
        return userDetailsService.createUser(user);
    }
}
