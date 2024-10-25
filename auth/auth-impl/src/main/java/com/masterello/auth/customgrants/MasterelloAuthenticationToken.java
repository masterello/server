package com.masterello.auth.customgrants;

import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.user.value.MasterelloUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class MasterelloAuthenticationToken extends AbstractAuthenticationToken {

    private final MasterelloUser principal;
    private final String credentials;

    public MasterelloAuthenticationToken(SecurityUserDetails user) {
        super(user.getAuthorities());
        this.principal = user.toMasterelloUser();
        this.credentials = user.getPassword();
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public MasterelloUser getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getUuid().toString();
    }
}
