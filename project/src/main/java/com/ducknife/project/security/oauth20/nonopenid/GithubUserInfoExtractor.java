package com.ducknife.project.security.oauth20.nonopenid;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ducknife.project.security.oauth20.OAuth2UserInfo;
import com.ducknife.project.security.oauth20.OAuth2UserInfoExtractor;

@Component
public class GithubUserInfoExtractor implements OAuth2UserInfoExtractor {

    private final RestClient restClient = RestClient.create();

    @Override
    public String provider() {
        return "github";
    }

    @Override
    public OAuth2UserInfo extract(OAuth2User user, String accessToken) {
        String name = user.getAttribute("login");
        Object idAttr = user.getAttribute("id");
        String providerId = String.valueOf(idAttr);
        String email = user.getAttribute("email");
        boolean verified;

        if (email == null) {
            // Email để private trên GitHub -> phải gọi riêng API lấy email chính + trạng
            // thái verified
            GithubEmail primary = fetchPrimaryGithubEmail(accessToken);
            email = primary.email();
            verified = primary.verified();
        } else {
            verified = true; // email public trả sẵn trong response chính coi như đã xác thực
        }
        return new OAuth2UserInfo(email, name, providerId, verified);
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
