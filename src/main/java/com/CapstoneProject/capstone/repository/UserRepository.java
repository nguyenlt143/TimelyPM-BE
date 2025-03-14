package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
