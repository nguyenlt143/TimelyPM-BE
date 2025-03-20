package com.CapstoneProject.capstone.model;

import com.CapstoneProject.capstone.enums.GenderEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
public class UserProfile extends BaseEntity {
    private String avatarUrl;
    private String gender;
    private String phone;
    private String fullName;
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    User user;
    @OneToMany(mappedBy = "userProfile")
    private List<Project> projects;

}
