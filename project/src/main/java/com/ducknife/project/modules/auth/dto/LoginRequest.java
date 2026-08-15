package com.ducknife.project.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "{auth.username.notblank}")
    private String username;
    @NotBlank(message = "{auth.password.notblank}")
    private String password;
}
