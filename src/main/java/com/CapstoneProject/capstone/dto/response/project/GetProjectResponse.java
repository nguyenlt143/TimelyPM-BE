package com.CapstoneProject.capstone.dto.response.project;

import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.model.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetProjectResponse {
    private UUID id;
    private String name;
    private String image;
    private String status;
    private UUID userId;
    public GetProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.image = project.getImage();
        this.status = project.getStatus();
        this.userId = (project.getUserProfile() != null && project.getUserProfile().getUser() != null)
                ? project.getUserProfile().getUser().getId()
                : null;
    }
}
