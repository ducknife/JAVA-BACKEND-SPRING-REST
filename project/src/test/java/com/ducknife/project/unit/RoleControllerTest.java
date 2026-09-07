package com.ducknife.project.unit;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ducknife.project.modules.permission.Permission;
import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.role.RoleController;
import com.ducknife.project.modules.role.RoleService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoleController.class)
public class RoleControllerTest {

    @MockitoBean
    private RoleService roleService;

    @Autowired
    private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void layDanhSachRole() throws Exception {

        List<Role> roles = new ArrayList<>();
        Permission permission = Permission.builder()
                .id(1L)
                .name("dev:write").build();
        Set<Permission> permissions = Set.of(permission);
        Role role = Role.builder()
                .id(1L)
                .name("DEV")
                .permissions(permissions)
                .build();

        when(roleService.getRoles()).thenReturn(List.of(role));

        mockMvc.perform(get("/api/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))// hasSize()
            .andExpect(jsonPath("$.data[0].name").value("DEV"))
            .andExpect(jsonPath("$.data[0].permissions[0].name").value("dev1:write")); 
    }
}
