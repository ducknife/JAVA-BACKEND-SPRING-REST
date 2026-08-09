package com.ducknife.project.modules.auth.dto;

import com.ducknife.project.common.validation.passwordmatch.PasswordMatch;
import com.ducknife.project.common.validation.strongpassword.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@PasswordMatch // tự động so sánh newpassword với confirmpassword;
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @StrongPassword
    private String newPassword;
    @NotBlank
    private String confirmPassword;
    @NotBlank
    private String refreshToken;
}
