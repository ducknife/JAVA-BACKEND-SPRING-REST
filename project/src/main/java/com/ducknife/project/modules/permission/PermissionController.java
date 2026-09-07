package com.ducknife.project.modules.permission;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/permissions")
@RequiredArgsConstructor 
public class PermissionController {
    
    private final PermissionService permissionService;

    @GetMapping 
    public ResponseEntity<ApiResponse<List<Permission>>> showAllPermissions() {
        return ApiResponse.ok(permissionService.getPermissions());
    }
}
