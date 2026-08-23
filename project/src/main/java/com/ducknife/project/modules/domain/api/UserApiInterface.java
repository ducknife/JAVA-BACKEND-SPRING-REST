package com.ducknife.project.modules.domain.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.user.dto.UserResponse;

@RequestMapping("/api/users")
public interface UserApiInterface {

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @PageableDefault(page = 0, sort = "fullname", direction = Sort.Direction.DESC) Pageable pageable);

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByFullname(
            @RequestParam String keyword);

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id);
 
}
