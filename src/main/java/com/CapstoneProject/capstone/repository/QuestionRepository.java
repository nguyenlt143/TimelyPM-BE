package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @Query(value = "SELECT * FROM question WHERE active = true AND topic_id = :topicId", nativeQuery = true)
    List<Question> findByTopicId(@Param("topicId") UUID topicId);

    @Query(value = """
            SELECT q.label
            FROM question q
            WHERE q.topic_id = :topicId
            ORDER BY q.label DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findMaxQuestionLabelByTopicId(@Param("topicId") UUID topicId);
}
