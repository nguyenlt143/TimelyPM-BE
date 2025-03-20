package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
}
