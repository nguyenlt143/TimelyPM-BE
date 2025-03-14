package com.CapstoneProject.capstone.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private boolean active;
    @CreatedDate
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
