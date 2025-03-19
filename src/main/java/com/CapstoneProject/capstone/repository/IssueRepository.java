package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
}
