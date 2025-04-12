package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @Query(value = "SELECT * FROM feedback WHERE active = true", nativeQuery = true)
    List<Feedback> findAll();

    @Query(value = "SELECT * FROM feedback WHERE active = true AND id = :id", nativeQuery = true)
    Optional<Feedback> findById(@Param("id") UUID id);
}
