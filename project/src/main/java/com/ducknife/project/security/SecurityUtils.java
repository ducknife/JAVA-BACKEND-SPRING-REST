package com.ducknife.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.ducknife.project.common.exception.UnauthorizedException;

public class SecurityUtils {

    public static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null 
            || !authentication.isAuthenticated() 
            || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return jwt;
    }

    public static String getCurrentSubject() {
        return getCurrentJwt().getSubject();
    }

    public static String getCurrentUserId() {
        return getCurrentJwt().getClaimAsString("userId");
    }
}
