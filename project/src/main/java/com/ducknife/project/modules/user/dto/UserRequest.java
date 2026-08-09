package com.ducknife.project.modules.user.dto;

import java.util.Set;

import com.ducknife.project.common.validation.group.OnCreate;
import com.ducknife.project.common.validation.group.OnUpdate;
import com.ducknife.project.common.validation.strongpassword.StrongPassword;
import com.ducknife.project.common.validation.uniqueusername.UniqueUsername;

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
    @NotBlank(message = "Tên không được để trống", groups = { OnUpdate.class, OnCreate.class })
    private String fullname;
    @UniqueUsername(groups = OnCreate.class)
    @NotBlank(message = "Tài khoản không được để trống", groups = { OnCreate.class, OnUpdate.class })
    private String username;
    @StrongPassword(groups = OnCreate.class)
    private String password;
    @NotNull(message = "Role không được để trống", groups = { OnCreate.class, OnUpdate.class })
    private Set<String> roles;
}
