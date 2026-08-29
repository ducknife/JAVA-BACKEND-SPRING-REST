package com.ducknife.project.security.oauth20;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {
    String provider();
    OAuth2UserInfo extract(OAuth2User user, String accessToken);
}
