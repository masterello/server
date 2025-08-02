package com.masterello.user.config;

import com.masterello.user.service.AuthNService;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        @Override
        public MasterelloUser googleSignup(String email, String name, String lastName) {
            return MasterelloTestUser.builder()
                    .email(email)
                    .uuid(UUID.randomUUID())
                    .name(name)
                    .lastname(lastName)
                    .roles(Set.of(Role.USER))
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
        }
    }
}
