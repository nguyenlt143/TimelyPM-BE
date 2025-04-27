package com.CapstoneProject.capstone.repository;

import com.CapstoneProject.capstone.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query(value = "SELECT * FROM notifications WHERE user_id = :userId AND active = true", nativeQuery = true)
    List<Notification> findByUserIdAndActive(@Param("userId") UUID userId);
}
