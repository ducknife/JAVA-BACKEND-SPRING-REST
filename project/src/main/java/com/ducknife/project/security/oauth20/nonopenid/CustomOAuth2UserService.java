package com.ducknife.project.security.oauth20.nonopenid;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ducknife.project.modules.user.User;
import com.ducknife.project.security.oauth20.OAuth2AccountLinker;

import lombok.RequiredArgsConstructor;

// Dùng cho provider OAuth2 thuần (không có scope "openid"), ví dụ GitHub.
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2AccountLinker accountLinker;
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "github"

        String name = oAuth2User.getAttribute("login");
        Object idAttr = oAuth2User.getAttribute("id");
        String providerId = String.valueOf(idAttr);
        String email = oAuth2User.getAttribute("email");
        boolean verified;

        if (email == null) {
            // Email để private trên GitHub -> phải gọi riêng API lấy email chính + trạng
            // thái verified
            GithubEmail primary = fetchPrimaryGithubEmail(userRequest.getAccessToken().getTokenValue());
            email = primary.email();
            verified = primary.verified();
        } else {
            verified = true; // email public trả sẵn trong response chính coi như đã xác thực
        }

        User user = accountLinker.findOrCreateUser(email, name, providerId, registrationId.toUpperCase(), verified);
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private GithubEmail fetchPrimaryGithubEmail(String accessToken) {
        List<GithubEmail> emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GithubEmail>>() {
                });

        return emails.stream()
                .filter(GithubEmail::primary)
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException("Không lấy được email từ GitHub"));
    }

    private record GithubEmail(String email, boolean primary, boolean verified) {
    }
}
