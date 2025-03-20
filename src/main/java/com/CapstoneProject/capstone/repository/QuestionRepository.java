package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}
