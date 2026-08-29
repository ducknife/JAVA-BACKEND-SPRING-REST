package com.ducknife.project.security.oauth20;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2UserInfo {
    private String email;
    private String name;
    private String providerId;
    private boolean emailVerified;
}
