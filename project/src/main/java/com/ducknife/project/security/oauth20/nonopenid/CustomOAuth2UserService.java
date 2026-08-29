package com.ducknife.project.security.oauth20.nonopenid;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ducknife.project.modules.user.User;
import com.ducknife.project.security.oauth20.OAuth2AccountLinker;
import com.ducknife.project.security.oauth20.OAuth2UserInfo;
import com.ducknife.project.security.oauth20.OAuth2UserInfoExtractorFactory;

import lombok.RequiredArgsConstructor;

// Dùng cho provider OAuth2 thuần (không có scope "openid"), ví dụ GitHub.
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2AccountLinker accountLinker;
    private final OAuth2UserInfoExtractorFactory extractorFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // Lấy thông tin user 
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "github, facebook, ..."

        // lấy thông tin qua factory
        OAuth2UserInfo info = extractorFactory.get(registrationId)
                .extract(oAuth2User, userRequest.getAccessToken().getTokenValue());

        // Tạo hoặc liên kết user 
        User user = accountLinker.findOrCreateUser(info.getEmail(), info.getName(), info.getProviderId(), registrationId.toUpperCase(), info.isEmailVerified());
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
