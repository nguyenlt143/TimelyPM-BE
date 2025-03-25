package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    @Query(value = "SELECT * FROM topic WHERE active = true AND project_id = :projectId", nativeQuery = true)
    List<Topic> findAll(@Param("projectId") UUID projectId);
    @Query(value = "SELECT * FROM topic WHERE id = :topicId AND project_id = :projectId AND active = true", nativeQuery = true)
    Optional<Topic> getByIdTopic(@Param("topicId") UUID topicId, @Param("projectId") UUID projectId);
}
