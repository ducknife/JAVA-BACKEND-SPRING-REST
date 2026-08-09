package com.ducknife.project.common.validation.uniqueusername;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.user.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
    
    private final UserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null) return true; // not blank sẽ lo phần này
        return !userRepository.existsByUsername(username);
    }
}
