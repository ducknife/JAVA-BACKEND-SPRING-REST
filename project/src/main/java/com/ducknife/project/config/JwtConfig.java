package com.ducknife.project.config;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.ducknife.project.config.properties.JwtProperties;
import com.ducknife.project.security.TokenBlacklistService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {
    private final JwtProperties jwtProps;
    private final TokenBlacklistService tokenBlacklistService;

    // encode jwt -> trả về 1 cái máy mẫ hoá với thuật toán HmacSHA256 
    // Do là khoá đối xứng nên dùng OctetSequenceKey 
    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] secretKeyBytes = Base64.getDecoder().decode(jwtProps.getSecretKey());
        SecretKey key = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        JWK jwk = new OctetSequenceKey.Builder(key)
                .algorithm(JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    // decode jwt -> trả về 1 cái máy giải mã với thuật toán HmacSHA256 
    // khi giải mã xong -> kiểm tra xem có trong black list không
    // nếu có thì báo lỗi luôn
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretKeyBytes = Base64.getDecoder().decode(jwtProps.getSecretKey());
        SecretKey key = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();

        // kiểm tra xem có trong black list không
        OAuth2TokenValidator<Jwt> notRevoked = jwt -> tokenBlacklistService.isRevoked(jwt.getId())
                ? OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("token_revoked", "Token đã bị thu hồi (logout)", null)
                ) 
                : OAuth2TokenValidatorResult.success();

        // set validator để check blacklist
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), notRevoked));
        return decoder;
    }

    // Converter role, permission
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            String scope = jwt.getClaimAsString("scope");
            if (scope != null) {
                Arrays.stream(scope.split(" "))
                    .map(s -> new SimpleGrantedAuthority(s))
                    .forEach(s -> authorities.add(s));
            }
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream()
                    .map(r -> new SimpleGrantedAuthority(r))
                    .forEach(r -> authorities.add(r));
            }
            return authorities;
        });

        return converter;
    }
}
