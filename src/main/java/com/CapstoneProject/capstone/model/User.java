package com.CapstoneProject.capstone.model;

import jakarta.persistence.Entity;
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
public class User extends BaseEntity{
    private String username;
    private String password;
    private String email;
    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;
    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;
}
