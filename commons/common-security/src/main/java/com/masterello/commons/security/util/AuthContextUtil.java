package com.masterello.commons.security.util;

import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.security.data.MasterelloAuthentication;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class AuthContextUtil {

    public AuthData getCurrentUserAuthContext(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return ((MasterelloAuthentication) securityContext.getAuthentication()).getDetails();
    }

    public boolean isAdmin() {
        return getCurrentUserAuthContext().getUserRoles().contains(AuthZRole.ADMIN);
    }
}
