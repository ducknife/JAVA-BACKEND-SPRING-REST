package com.ducknife.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("perm")
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;

    public boolean canUpdateUser(Long userId, Authentication authentication) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        
        boolean isPrivilege = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_COLLABORATOR"));

        return isSelf(userId, authentication) || isPrivilege;
    }

    public boolean isSelf(Long userId, Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return userId != null && userId.toString().equals(jwt.getClaimAsString("userId"));
        }
        return false;
    }
}
