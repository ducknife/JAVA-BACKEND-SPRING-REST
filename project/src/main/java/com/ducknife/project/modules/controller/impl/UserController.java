package com.ducknife.project.modules.controller.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.controller.api.UserApiInterface;
import com.ducknife.project.modules.user.UserService;
import com.ducknife.project.modules.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

// @RestController
@RequiredArgsConstructor
public class UserController implements UserApiInterface {

    private final UserService userService;

    @Override
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @PageableDefault(page = 0, sort = "fullname", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(userService.getUsers(pageable));
    }

    @Override
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByFullname(
            @RequestParam String keyword) {
        return ApiResponse.ok(userService.getUserByFullname(keyword));
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(userService.getMe(jwt));
    }
}
