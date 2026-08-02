package com.ducknife.project.security.oauth20;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ducknife.project.config.properties.CorsProperties;
import com.ducknife.project.modules.auth.dto.AuthResponse;
import com.ducknife.project.security.AppUserDetails;
import com.ducknife.project.security.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final OAuth2ExchangeStore exchangeStore;
    private final CorsProperties corsProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();

        AuthResponse tokens = AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(jwtService.generateRefreshToken(principal))
                .expiresIn(3600L)
                .build();

        String code = exchangeStore.save(tokens);

        String targetUrl = UriComponentsBuilder.fromUriString(corsProperties.getFrontendCallbackUrl())
                .queryParam("code", code)
                .build().toUriString();
    
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
