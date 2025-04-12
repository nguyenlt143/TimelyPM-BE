package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Issue;
import com.CapstoneProject.capstone.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    @Query(value = "SELECT * FROM issue WHERE active = true AND topic_id = :topicId", nativeQuery = true)
    List<Issue> findByTopicId(@Param("topicId") UUID topicId);

    @Query(value = "SELECT * FROM issue WHERE active = true AND task_id = :taskId AND id = :id", nativeQuery = true)
    Optional<Issue> findByIdAndTaskId(@Param("id") UUID id, @Param("taskId") UUID taskId);

    @Query(value = """
            SELECT t.label
            FROM issue t
            WHERE t.topic_id = :topicId
            ORDER BY t.label DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findMaxIssueLabelByTopicId(@Param("topicId") UUID topicId);

    @Query(value = "SELECT * FROM issue WHERE active = true AND task_id = :taskId", nativeQuery = true)
    List<Issue> findAllByTaskId(@Param("taskId") UUID taskId);

    @Query(value = """
    SELECT t.*
    FROM issue t
    JOIN topic tp ON t.topic_id = tp.id
    WHERE t.active = true
      AND tp.project_id = :projectId
    """, nativeQuery = true)
    List<Issue> findByProjectId(@Param("projectId") UUID projectId);


}
