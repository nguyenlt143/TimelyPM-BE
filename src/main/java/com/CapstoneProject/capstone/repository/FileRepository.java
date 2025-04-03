package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.File;
import com.CapstoneProject.capstone.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    @Query(value = "SELECT * FROM file WHERE active = true AND task_id = :taskId", nativeQuery = true)
    List<File> findByTaskId(@Param("taskId") UUID taskId);
}
