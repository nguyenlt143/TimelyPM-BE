package com.CapstoneProject.capstone.model;

import com.CapstoneProject.capstone.enums.MemberStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProjectMember extends BaseEntity{
    @Enumerated(EnumType.STRING)
    private MemberStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "assignee")
    private List<Task> tasks;

    @OneToMany(mappedBy = "assignee")
    private List<Issue> issues;

    @OneToMany(mappedBy = "assignee")
    private List<Question> questions;

    @OneToMany(mappedBy = "projectMember")
    private List<File> files;
}
