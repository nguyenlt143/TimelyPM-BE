package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
