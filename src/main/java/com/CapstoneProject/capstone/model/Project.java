package com.CapstoneProject.capstone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Project extends BaseEntity {
    private String name;
    private String image;
    private String status;
    private String code;
    @OneToMany(mappedBy = "project")
    private List<Notification> notifications;
    @OneToMany(mappedBy = "project")
    private List<Topic> topics;
    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMembers;
    @OneToMany(mappedBy = "project")
    private List<File> files;
    @ManyToOne()
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private UserProfile userProfile;
}
