package com.ducknife.project.modules.user.dto;

import java.util.Set;

import com.ducknife.project.common.validation.group.BasicCheck;
import com.ducknife.project.common.validation.group.DbCheck;
import com.ducknife.project.common.validation.group.OnUpdate;
import com.ducknife.project.common.validation.strongpassword.StrongPassword;
import com.ducknife.project.common.validation.uniqueusername.UniqueUsername;
import com.ducknife.project.common.validation.username.Username;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "{user.fullname.notblank}", groups = { OnUpdate.class, BasicCheck.class })
    private String fullname;
    @UniqueUsername(groups = { DbCheck.class })
    @Username(groups = { BasicCheck.class, OnUpdate.class })
    private String username;
    @StrongPassword(groups = BasicCheck.class)
    private String password;
    @NotNull(message = "{user.roles.notnull}", groups = { BasicCheck.class, OnUpdate.class })
    private Set<String> roles;
}
