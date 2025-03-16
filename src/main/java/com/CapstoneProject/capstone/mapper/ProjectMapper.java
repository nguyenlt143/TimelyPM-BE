package com.CapstoneProject.capstone.mapper;

import com.CapstoneProject.capstone.dto.request.project.CreateNewProjectRequest;
import com.CapstoneProject.capstone.dto.response.project.CreateNewProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.UpdateProjectResponse;
import com.CapstoneProject.capstone.model.Project;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMapper {
    ModelMapper modelMapper;
    public Project toProject(CreateNewProjectRequest createNewProjectRequest) {
        return modelMapper.map(createNewProjectRequest, Project.class);
    }

    public CreateNewProjectResponse toResponse(Project project) {
        return modelMapper.map(project, CreateNewProjectResponse.class);
    }

    public GetProjectResponse toGetResponse(Project project){
        return modelMapper.map(project, GetProjectResponse.class);
    }

    public UpdateProjectResponse toUpdateResponse(Project project) {
        return modelMapper.map(project, UpdateProjectResponse.class);
    }
}
