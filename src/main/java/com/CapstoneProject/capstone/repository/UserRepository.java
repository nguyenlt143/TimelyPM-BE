package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(value = "SELECT * FROM user WHERE active = true", nativeQuery = true)
    List<User> findAll();
    @Query(value = "SELECT * FROM user WHERE username = :username AND active = true", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);
    @Query(value = "SELECT * FROM user WHERE email = :email AND active = true", nativeQuery = true)
    Optional<User> findByEmail(String email);
    @Query(value = "SELECT * FROM user WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailActive(String email);
    @Query(value = """
        SELECT u.* FROM project_member pm
        JOIN `user` u ON pm.user_id = u.id
        JOIN `role` r ON pm.role_id = r.id
        WHERE pm.project_id = :projectId
        AND r.name = 'PM'
        LIMIT 1
    """, nativeQuery = true)
    Optional<User> findUserWithRolePMByProjectId(@Param("projectId") UUID projectId);

    @Query(value = """
        SELECT u.* FROM project_member pm
        JOIN `user` u ON pm.user_id = u.id
        JOIN `role` r ON pm.role_id = r.id
        WHERE pm.project_id = :projectId
        AND r.name IN ('PM', 'QA')
    """, nativeQuery = true)
    List<User> findUsersWithRolePMOrQAByProjectId(@Param("projectId") UUID projectId);

    List<User> findAllById(Iterable<UUID> ids);

}
