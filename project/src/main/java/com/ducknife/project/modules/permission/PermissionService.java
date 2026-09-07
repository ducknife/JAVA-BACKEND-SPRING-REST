package com.ducknife.project.modules.permission;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class PermissionService {
    
    private final PermissionRepository permissionRepository;

    public List<Permission> getPermissions() {
        return permissionRepository.findAll();
    }
}
