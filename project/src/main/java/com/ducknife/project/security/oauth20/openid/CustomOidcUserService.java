package com.ducknife.project.security.oauth20.openid;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.ducknife.project.modules.user.User;
import com.ducknife.project.security.oauth20.OAuth2AccountLinker;
import com.ducknife.project.security.oauth20.OAuth2UserInfo;
import com.ducknife.project.security.oauth20.OAuth2UserInfoExtractorFactory;

import lombok.RequiredArgsConstructor;

// Google chạy theo chuẩn OIDC (scope có "openid") nên phải kế thừa OidcUserService,
// không phải DefaultOAuth2UserService (chỉ dùng cho provider OAuth2 thuần, không OIDC).
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2AccountLinker accountLinker;
    private final OAuth2UserInfoExtractorFactory extractorFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"

        OAuth2UserInfo info = extractorFactory.get(registrationId)
                .extract(oidcUser, userRequest.getAccessToken().getTokenValue());
        User user = accountLinker.findOrCreateUser(info.getEmail(), info.getName(), info.getProviderId(),
        
                registrationId.toUpperCase(), info.isEmailVerified());
        return new CustomOidcUser(user, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
