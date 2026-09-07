package com.ducknife.project.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ducknife.project.modules.permission.Permission;
import com.ducknife.project.modules.permission.PermissionController;
import com.ducknife.project.modules.permission.PermissionService;

@WebMvcTest(PermissionController.class)
public class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @DisplayName("Lấy danh sách authorites")
    public void layDanhSachAuthorities() throws Exception {

        Permission permission = Permission.builder()
                .id(1L)
                .name("dev:read")
                .build();

        List<Permission> permissions = List.of(permission);
 
        when(permissionService.getPermissions()).thenReturn(permissions);

        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isFound());
                // .andExpect(jsonPath("$.data[0].name").value("dev:read"));
    }
}
