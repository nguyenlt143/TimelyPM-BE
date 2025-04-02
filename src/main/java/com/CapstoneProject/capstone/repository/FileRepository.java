package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
}
