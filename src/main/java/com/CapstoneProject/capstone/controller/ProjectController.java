package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.dto.request.project.CreateNewProjectRequest;
import com.CapstoneProject.capstone.dto.request.project.UpdateProjectRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.project.CreateNewProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.UpdateProjectResponse;
import com.CapstoneProject.capstone.model.Project;
import com.CapstoneProject.capstone.service.impl.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<CreateNewProjectResponse>> create(@Valid @RequestBody CreateNewProjectRequest request) {
        CreateNewProjectResponse response = projectService.createNewProject(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo dự án thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GetProjectResponse>> getProject(@PathVariable UUID id) {
        GetProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tìm dự án thành công", response));
    }

    @GetMapping()
    public ResponseEntity<BaseResponse<List<GetProjectResponse>>> getProjects() {
        List<GetProjectResponse> response = projectService.getAllProjects();
        return ResponseEntity.ok(new BaseResponse<>("200", "Danh sách dự án", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Boolean>> delete(@PathVariable UUID id) {
        boolean response = projectService.deleteProjectById(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa dự án thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<UpdateProjectResponse>> update(@PathVariable UUID id, @RequestBody UpdateProjectRequest request) {
        UpdateProjectResponse response = projectService.updateProjectById(id, request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Cập nhật dự án thành công", response));
    }
}
