package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Query(value = """
        SELECT u.* FROM project_member pm
        JOIN `user` u ON pm.user_id = u.id
        JOIN `role` r ON pm.role_id = r.id
        WHERE pm.project_id = :projectId
        AND r.name = 'PM'
        LIMIT 1
    """, nativeQuery = true)
    Optional<User> findUserWithRolePMByProjectId(@Param("projectId") UUID projectId);
}
