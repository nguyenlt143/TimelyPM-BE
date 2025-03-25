package com.CapstoneProject.capstone.repository;

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


}
