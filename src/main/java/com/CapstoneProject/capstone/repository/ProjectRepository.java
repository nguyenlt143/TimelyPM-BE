package com.CapstoneProject.capstone.repository;

import ch.qos.logback.core.status.Status;
import com.CapstoneProject.capstone.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query(value = "SELECT * FROM project WHERE active = true", nativeQuery = true)
    List<Project> getAllProjects();
    @Query(value = "SELECT * FROM project WHERE active = true AND id = :id", nativeQuery = true)
    Optional<Project> findById(@Param("id") UUID id);
    @Query(value = """
        SELECT DISTINCT p.* FROM project p
        LEFT JOIN project_member pm ON p.id = pm.project_id
        LEFT JOIN user_profile up ON p.profile_id = up.id
        WHERE (pm.user_id = :userId OR up.user_id = :userId)
        AND p.active = true
    """, nativeQuery = true)
    List<Project> findByUser(@Param("userId") UUID userId);

    @Query(value = "SELECT * FROM project WHERE active = true AND code = :code", nativeQuery = true)
    Optional<Project> findByCode(@Param("code") String code);

    @Query(value = "SELECT COUNT(*) FROM Project WHERE status = :status AND active = true", nativeQuery = true)
    int countByStatus(@Param("status") String status);

    @Query(value = """
        SELECT p.*
        FROM project p
        JOIN project_member pm ON p.id = pm.project_id
        WHERE pm.user_id = :userId
        AND pm.status = 'APPROVED'
        AND p.active = true
        """, nativeQuery = true)
    List<Project> getApprovedProjectsByUserId(@Param("userId") UUID userId);
}
