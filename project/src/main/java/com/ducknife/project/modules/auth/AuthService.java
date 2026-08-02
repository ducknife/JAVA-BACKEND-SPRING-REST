package com.ducknife.project.modules.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.InvalidRequestException;
import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.common.exception.UnauthorizedException;
import com.ducknife.project.config.properties.JwtProperties;
import com.ducknife.project.modules.auth.dto.AuthResponse;
import com.ducknife.project.modules.auth.dto.ChangePasswordRequest;
import com.ducknife.project.modules.auth.dto.LoginRequest;
import com.ducknife.project.modules.auth.dto.LogoutRequest;
import com.ducknife.project.modules.auth.dto.RefreshTokenRequest;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;
import com.ducknife.project.security.TokenBlacklistService;
import com.ducknife.project.security.jwt.CustomUserDetails;
import com.ducknife.project.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProps;
    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse checkLogin(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProps.getAccessTokenExpiration())
                .build();
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        Jwt refreshToken;
        try {
            refreshToken = jwtDecoder.decode(request.getRefreshToken());
        } catch (JwtException e) {
            throw new UnauthorizedException("Refresh Token không hợp lệ hoặc hết hạn!");
        }

        if (!"refresh".equals(refreshToken.getClaimAsString("type"))) {
            throw new UnauthorizedException("Token không phải refresh");
        }

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                .loadUserByUsername(refreshToken.getSubject());

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .expiresIn(jwtProps.getAccessTokenExpiration())
                .build();
    }

    @Transactional
    public void logout(Jwt accessToken, LogoutRequest request) {
        tokenBlacklistService.revoke(accessToken.getId(), accessToken.getExpiresAt());

        try {
            Jwt refreshToken = jwtDecoder.decode(request.getRefreshToken());
            tokenBlacklistService.revoke(refreshToken.getId(), refreshToken.getExpiresAt());
        } catch (JwtException e) {

        }
    }

    @Transactional
    public void changePassword(Jwt accessToken, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(accessToken.getSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tìm thấy người dùng!"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("Mật khẩu cũ không đúng!");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Mật khẩu xác nhận không đúng!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenBlacklistService.revoke(accessToken.getId(), accessToken.getExpiresAt());
        try {
            Jwt refreshToken = jwtDecoder.decode(request.getRefreshToken());
            tokenBlacklistService.revoke(refreshToken.getId(), refreshToken.getExpiresAt());
        } catch (JwtException e) {

        }
    }
}
