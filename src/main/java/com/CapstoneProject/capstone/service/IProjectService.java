package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.project.CreateNewProjectRequest;
import com.CapstoneProject.capstone.dto.request.project.UpdateProjectRequest;
import com.CapstoneProject.capstone.dto.response.project.CreateNewProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.UpdateProjectResponse;
import com.CapstoneProject.capstone.enums.RoleEnum;

import java.util.List;
import java.util.UUID;

public interface IProjectService {
    CreateNewProjectResponse createNewProject(CreateNewProjectRequest request);
    List<GetProjectResponse> getAllProjects();
    GetProjectResponse getProjectById(UUID id);
    boolean deleteProjectById(UUID id);
    UpdateProjectResponse updateProjectById(UUID id, UpdateProjectRequest request);
    boolean inviteUserToProject(UUID projectId, String email, String role);
    boolean deleteUserFromProject(UUID projectId, UUID id);
}
