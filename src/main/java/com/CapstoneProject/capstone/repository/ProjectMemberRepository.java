package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.ProjectMember;
import com.CapstoneProject.capstone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    @Query(value = """
        SELECT * FROM project_member pm
        WHERE pm.project_id = :projectId
        AND pm.user_id = :userId
        LIMIT 1
    """, nativeQuery = true)
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    @Query(value = "SELECT * FROM project_member WHERE active = true AND id = :id", nativeQuery = true)
    Optional<ProjectMember> findProjectMember(@Param("id") UUID id);

    @Query(value = "SELECT * FROM project_member WHERE project_id = :projectId", nativeQuery = true)
    List<ProjectMember> findByProjectId(@Param("projectId") UUID projectId);
}
