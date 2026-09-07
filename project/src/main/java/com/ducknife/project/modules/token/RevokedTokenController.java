package com.ducknife.project.modules.token;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/revoked-tokens")
@RequiredArgsConstructor 
public class RevokedTokenController {
    
    private final RevokedTokenService revokedTokenService;

    @GetMapping 
    public ResponseEntity<ApiResponse<List<RevokedToken>>> getRevokedTokens() {
        return ApiResponse.ok(revokedTokenService.getRevokedTokens());
    }
    
}
