package com.ducknife.project.modules.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.dto.UserRequest;
import com.ducknife.project.modules.user.dto.UserResponse;

@Mapper(componentModel = "spring") // map struct tự sinh ra 1 class implements interface này
public interface UserMapper {
    @Mapping(source = "id", target = "userId")
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))")
    UserResponse toResponse(User user);

    @Mapping(target = "roles", ignore = true)
    User toEntity(UserRequest request);
}
