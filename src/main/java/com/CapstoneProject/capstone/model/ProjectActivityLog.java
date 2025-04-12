package com.CapstoneProject.capstone.model;

import com.CapstoneProject.capstone.enums.ActivityTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    private ActivityTypeEnum activityType;
    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;
}
