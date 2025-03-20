package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.ProjectMember;
import com.CapstoneProject.capstone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
