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

    @Query(value = """
            SELECT MAX(CAST(SUBSTRING_INDEX(t.label, ' ', -1) AS UNSIGNED))
            FROM task t WHERE t.topic_id = :topicId
            """, nativeQuery = true)
    Optional<Integer> findMaxTaskNumberByTopicId(@Param("topicId") UUID topicId);


}
