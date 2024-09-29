package com.masterello.user.config;

import com.masterello.user.service.AuthNService;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Configuration
public class UserContractTestConfig {

    @Bean
    public MasterelloUserService masterelloUserService() {
        return mock(MasterelloUserService.class);
    }

    @Bean
    public AuthNService authNService() {
        return spy(new TestAuthNService());
    }

    private static class TestAuthNService implements AuthNService {

        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        @Override
        public MasterelloUser googleSignup(String email) {
            return MasterelloTestUser.builder()
                    .email(email)
                    .uuid(UUID.randomUUID())
                    .roles(Set.of(Role.USER))
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
        }

        @Override
        public boolean checkPassword(String rawPassword, String encodedPassword) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
    }
}
