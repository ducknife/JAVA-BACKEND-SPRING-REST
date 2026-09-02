package com.ducknife.project.security.oauth20.openid;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ducknife.project.security.oauth20.OAuth2UserInfo;
import com.ducknife.project.security.oauth20.OAuth2UserInfoExtractor;

public class GoogleUserInfoExtractor implements OAuth2UserInfoExtractor {

    @Override
    public String provider() {
        return "google";
    }

    @Override
    public OAuth2UserInfo extract(OAuth2User user, String accessToken) {
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        String providerId = user.getAttribute("sub");
        boolean verified = Boolean.TRUE.equals(user.getAttribute("email_verified"));
        return new OAuth2UserInfo(email, name, providerId, verified);
    }
}
