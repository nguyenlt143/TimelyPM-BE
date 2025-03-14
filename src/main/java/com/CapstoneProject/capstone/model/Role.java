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
public class Role extends BaseEntity {
    private String name;
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles;
}
