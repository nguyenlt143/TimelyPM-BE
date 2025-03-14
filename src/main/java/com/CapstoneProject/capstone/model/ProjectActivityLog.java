package com.CapstoneProject.capstone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ProjectActivityLog extends BaseEntity{
    private String activityType;
    private String details;
    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;
}
