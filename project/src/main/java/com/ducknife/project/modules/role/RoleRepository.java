package com.ducknife.project.modules.role;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>{
    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByName(String name);
}
