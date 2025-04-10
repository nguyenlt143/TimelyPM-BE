package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query(value = "SELECT * FROM comment WHERE active = true AND question_id = :questionId", nativeQuery = true)
    List<Comment> findByQuestionId(@Param("questionId") UUID questionId);
}
