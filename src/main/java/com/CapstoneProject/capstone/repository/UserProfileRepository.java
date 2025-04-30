package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);
    @Query("SELECT p FROM UserProfile p WHERE p.user.id IN :userIds")
    List<UserProfile> findAllByUserIdIn(@Param("userIds") Iterable<UUID> userIds);
}
