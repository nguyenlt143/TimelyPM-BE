package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
}
