package com.masterello.data;

import com.masterello.auth.data.AuthData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@RequiredArgsConstructor
public class MasterelloAuthentication implements Authentication {

    private final AuthData authData;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public AuthData getDetails() {
        return authData;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Cannot set MasterelloAuthentication as authenticated");
    }

    @Override
    public String getName() {
        return authData.getUsername();
    }
}
