package com.masterello.auth.service;

import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.user.service.MasterelloUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final MasterelloUserService userService;

    @Override
    public SecurityUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByEmail(username)
                .map(SecurityUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    public boolean existsByEmail(String email) {
        return userService.existsByEmail(email);
    }
}
