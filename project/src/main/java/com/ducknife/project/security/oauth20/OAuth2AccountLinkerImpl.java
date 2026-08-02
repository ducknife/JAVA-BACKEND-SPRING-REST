package com.ducknife.project.security.oauth20;

import java.util.Set;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.role.RoleRepository;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2AccountLinkerImpl implements OAuth2AccountLinker {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User findOrCreateUser(String email, String name, String providerId,
                                   String providerName, boolean emailVerified) {
        if (!emailVerified) {
            throw new OAuth2AuthenticationException("Email chưa được " + providerName + " xác thực");
        }

        User user = userRepository.findByUsername(email).orElse(null);

        if (user != null && !providerName.equals(user.getProvider())) {
            // đã có người đăng kí bằng email này (tài khoản local hoặc provider khác) -> từ chối
            throw new OAuth2AuthenticationException("Email đã được đăng ký bằng phương thức khác");
        }

        if (user == null) {
            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role chưa có trong DB!"));
            user = User.builder()
                    .username(email)
                    .fullname(name)
                    .email(email)
                    .provider(providerName)
                    .providerId(providerId)
                    .roles(Set.of(role))
                    .build();
            userRepository.save(user);
        }
        return user;
    }
}
