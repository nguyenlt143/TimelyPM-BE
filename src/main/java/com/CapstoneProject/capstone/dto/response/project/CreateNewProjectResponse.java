package com.CapstoneProject.capstone.dto.response.project;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNewProjectResponse {
    private UUID id;
    private String name;
    private String image;
    private String status;
}
