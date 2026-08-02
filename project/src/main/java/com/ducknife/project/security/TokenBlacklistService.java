package com.ducknife.project.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.modules.token.RevokedToken;
import com.ducknife.project.modules.token.RevokedTokenRepository;

import lombok.RequiredArgsConstructor;

/* revoke token và check xem cái nào đã bị revoke */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void revoke(String jti, Instant expiresAt) {
        revokedTokenRepository.save(
                RevokedToken.builder()
                        .jti(jti)
                        .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()))
                        .build());
    }

    public boolean isRevoked(String jti) {
        return revokedTokenRepository.existsById(jti);
    }
}
