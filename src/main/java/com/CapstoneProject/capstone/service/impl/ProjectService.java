package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.project.CreateNewProjectRequest;
import com.CapstoneProject.capstone.dto.request.project.UpdateProjectRequest;
import com.CapstoneProject.capstone.dto.response.project.CreateNewProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.UpdateProjectResponse;
import com.CapstoneProject.capstone.enums.ProjectStatusEnum;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.mapper.ProjectMapper;
import com.CapstoneProject.capstone.model.Project;
import com.CapstoneProject.capstone.repository.ProjectRepository;
import com.CapstoneProject.capstone.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;

    @Override
    public CreateNewProjectResponse createNewProject(CreateNewProjectRequest request) {
        String statusSt = request.getStatus();
        try{
            ProjectStatusEnum status = ProjectStatusEnum.valueOf(statusSt.toUpperCase());
        }catch (IllegalArgumentException | NullPointerException e){
            throw new InvalidEnumException("Trạng thái không hợp lệ");
        }

        Project project = projectMapper.toProject(request);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setActive(true);
        projectRepository.save(project);
        CreateNewProjectResponse response = projectMapper.toResponse(project);
        return response;
    }

    @Override
    public List<GetProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.getAllProjects();
        return projects.stream().map(projectMapper::toGetResponse).collect(Collectors.toList());
    }

    @Override
    public GetProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        return projectMapper.toGetResponse(project);
    }

    @Override
    public boolean deleteProjectById(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        project.setActive(false);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
        return true;
    }

    @Override
    public UpdateProjectResponse updateProjectById(UUID id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        if (request.getStatus() != null){
            String statusSt = request.getStatus();
            try{
                ProjectStatusEnum status = ProjectStatusEnum.valueOf(statusSt.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }
        project.setName(request.getName() == null ? project.getName() : request.getName());
        project.setImage(request.getImage() == null ? project.getImage() : request.getImage());
        project.setStatus(request.getStatus() == null ? project.getStatus() : request.getStatus());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
        UpdateProjectResponse response = projectMapper.toUpdateResponse(project);
        return response;
    }


}
