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
    @NotBlank(message = "{auth.oldpassword.notblank}")
    private String oldPassword;
    @NotBlank(message = "{auth.newpassword.notblank}")
    @StrongPassword
    private String newPassword;
    @NotBlank(message = "{auth.confirmpassword.notblank}")
    private String confirmPassword;
    @NotBlank(message = "{auth.refreshtoken.notblank}")
    private String refreshToken;
}
