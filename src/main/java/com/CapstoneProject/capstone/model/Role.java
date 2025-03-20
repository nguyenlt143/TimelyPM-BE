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
<<<<<<< HEAD
    @Column(columnDefinition = "ENUM('ADMIN', 'BA', 'DEV', 'PM', 'TESTER', 'USER')")
=======
    @Column(columnDefinition = "ENUM('ADMIN', 'QA', 'DEV', 'PM', 'USER')")
>>>>>>> a17adb759a5f60a26e573478b71627fc5b7fb7d8
    private RoleEnum name;
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles;
}
