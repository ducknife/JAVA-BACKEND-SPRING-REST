package com.ducknife.project.modules.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ResponseFactory;
import com.ducknife.project.modules.auth.dto.AuthResponse;
import com.ducknife.project.modules.auth.dto.ChangePasswordRequest;
import com.ducknife.project.modules.auth.dto.LoginRequest;
import com.ducknife.project.modules.auth.dto.LogoutRequest;
import com.ducknife.project.modules.auth.dto.RefreshTokenRequest;
import com.ducknife.project.security.oauth20.OAuth2ExchangeStore;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2ExchangeStore exchangeStore;

    @PostMapping("/login")
    public ResponseEntity<ResponseFactory<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseFactory.ok(authService.checkLogin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseFactory<AuthResponse>> getRefreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseFactory.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseFactory<String>> logout(@AuthenticationPrincipal Jwt accessToken, @RequestBody @Valid LogoutRequest request) {
        authService.logout(accessToken, request);
        return ResponseFactory.ok("Đăng xuất thành công!");
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseFactory<String>> changePassword(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid ChangePasswordRequest request
    ) {
        authService.changePassword(jwt, request);
        return ResponseFactory.ok("Đổi mật khẩu thành công, vui lòng đăng nhập lại!");
    }

    // FE đổi code lấy tokens thật
    @GetMapping("/oauth2/exchange")
    public ResponseEntity<ResponseFactory<AuthResponse>> exchange(@RequestParam String code) {
        return ResponseFactory.ok(exchangeStore.consume(code));
    }
}
