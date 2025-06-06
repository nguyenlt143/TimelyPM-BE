package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query(value = "SELECT * FROM task WHERE active = true AND topic_id = :topicId", nativeQuery = true)
    List<Task> findByTopicId(@Param("topicId") UUID topicId);

    @Query("SELECT t FROM Task t WHERE t.active = true AND t.topic.id = :topicId " +
            "AND (t.assignee.user.id = :userId OR t.reporter.user.id = :userId)")
    List<Task> findByTopicIdAndUserId(@Param("topicId") UUID topicId, @Param("userId") UUID userId);

    @Query(value = """
            SELECT t.label
            FROM task t
            WHERE t.topic_id = :topicId
            ORDER BY t.label DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findMaxTaskLabelByTopicId(@Param("topicId") UUID topicId);

    @Query(value = """
    SELECT t.*
    FROM task t
    JOIN topic tp ON t.topic_id = tp.id
    WHERE t.active = true
      AND tp.project_id = :projectId
    """, nativeQuery = true)
    List<Task> findByProjectId(@Param("projectId") UUID projectId);

}
