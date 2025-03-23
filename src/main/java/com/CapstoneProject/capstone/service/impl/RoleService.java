package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.model.Role;
import com.CapstoneProject.capstone.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(null, RoleEnum.ADMIN),
                    new Role(null, RoleEnum.QA),
                    new Role(null, RoleEnum.DEV),
                    new Role(null, RoleEnum.PM),
                    new Role(null, RoleEnum.USER)
            ));
        }
    }
}