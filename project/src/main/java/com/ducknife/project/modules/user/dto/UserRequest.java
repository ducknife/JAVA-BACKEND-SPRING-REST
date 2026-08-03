package com.ducknife.project.modules.user.dto;

import java.util.Set;

import com.ducknife.project.common.validation.strongpassword.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "Tên không được để trống")
    private String fullname;
    @NotBlank(message = "Tài khoản không được để trống")
    private String username;
    @StrongPassword
    private String password;
    @NotNull(message = "Role không được để trống")
    private Set<String> roles;
}
