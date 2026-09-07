package com.ducknife.project.modules.token;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
@Transactional(readOnly = true)
public class RevokedTokenService {
    
    private final RevokedTokenRepository revokedTokenRepository;

    public List<RevokedToken> getRevokedTokens() {
        return revokedTokenRepository.findAll();
    }
}
