package com.ducknife.project.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface AppUserDetails extends UserDetails {
    Long getUserId();
}
