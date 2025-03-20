package com.CapstoneProject.capstone.model;

import com.CapstoneProject.capstone.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Role{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ADMIN', 'BA', 'DEV', 'PM', 'TESTER', 'USER')")
    private RoleEnum name;
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles;
}
