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

    @Query("SELECT f FROM File f WHERE f.task.id IN :taskIds")
    List<File> findByTaskIds(@Param("taskIds") List<UUID> taskIds);

    @Query(value = "SELECT * FROM file WHERE project_id = :projectId AND active = true", nativeQuery = true)
    List<File> findByProjectId(@Param("projectId") UUID projectId);
}
