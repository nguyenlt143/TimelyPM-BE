package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.ProjectActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectActivityLogRepository extends JpaRepository<ProjectActivityLog, UUID> {
    @Query(value = "SELECT * FROM project_activity_log WHERE active = true AND project_id = :projectId", nativeQuery = true)
    List<ProjectActivityLog> findAll(@Param("projectId") UUID projectId);

    @Query(value = "SELECT * FROM project_activity_log WHERE project_id = :projectId AND active = true", nativeQuery = true)
    Optional<ProjectActivityLog> findByProjectId(@Param("projectId") UUID projectId);
}
