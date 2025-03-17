package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(RoleEnum roleEnum);
}
