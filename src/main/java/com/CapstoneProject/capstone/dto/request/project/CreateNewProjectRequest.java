package com.CapstoneProject.capstone.dto.request.project;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNewProjectRequest {
    @NotNull(message = "Tên dự án không được để trống")
    private String name;
    private String image;
    private String status;
}
