package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
}
