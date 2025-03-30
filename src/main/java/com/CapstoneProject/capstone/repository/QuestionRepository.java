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
            SELECT MAX(CAST(SUBSTRING_INDEX(t.label, ' ', -1) AS UNSIGNED))
            FROM question t WHERE t.topic_id = :topicId
            """, nativeQuery = true)
    Optional<Integer> findMaxQuestionNumberByTopicId(@Param("topicId") UUID topicId);
}
