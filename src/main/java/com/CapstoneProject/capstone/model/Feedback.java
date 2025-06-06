package com.CapstoneProject.capstone.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Feedback extends BaseEntity{
    private String feedback;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
