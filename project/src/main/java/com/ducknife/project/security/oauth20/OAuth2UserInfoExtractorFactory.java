package com.ducknife.project.security.oauth20;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserInfoExtractorFactory {

    private final Map<String, OAuth2UserInfoExtractor> extractors;

    public OAuth2UserInfoExtractorFactory(List<OAuth2UserInfoExtractor> list) {
        this.extractors = list.stream()
                .collect(Collectors.toMap(OAuth2UserInfoExtractor::provider, Function.identity()));
    }

    public OAuth2UserInfoExtractor get(String registrationId) {
        OAuth2UserInfoExtractor extractor = extractors.get(registrationId);
        if (extractor == null) {
            throw new OAuth2AuthenticationException("Chưa hỗ trợ phương thức đăng nhập qua " + registrationId);
        }
        return extractor;
    }
}
