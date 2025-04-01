package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.project.CreateNewProjectRequest;
import com.CapstoneProject.capstone.dto.request.project.UpdateProjectRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.project.CreateNewProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.UpdateProjectResponse;
import com.CapstoneProject.capstone.service.IProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.PROJECT.PROJECT)
@RequiredArgsConstructor
public class ProjectController {
    private final IProjectService projectService;

    @PostMapping(UrlConstant.PROJECT.CREATE)
    public ResponseEntity<BaseResponse<CreateNewProjectResponse>> create(@Valid @RequestBody CreateNewProjectRequest request) {
        CreateNewProjectResponse response = projectService.createNewProject(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo dự án thành công", response));
    }

    @GetMapping(UrlConstant.PROJECT.GET_PROJECT)
    public ResponseEntity<BaseResponse<GetProjectResponse>> getProject(@PathVariable UUID id) {
        GetProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tìm dự án thành công", response));
    }

    @GetMapping(UrlConstant.PROJECT.GET_PROJECTS)
    public ResponseEntity<BaseResponse<List<GetProjectResponse>>> getProjects() {
        List<GetProjectResponse> response = projectService.getAllProjects();
        return ResponseEntity.ok(new BaseResponse<>("200", "Danh sách dự án", response));
    }

    @GetMapping(UrlConstant.PROJECT.GET_PROJECTS_BY_USER)
    public ResponseEntity<BaseResponse<List<GetProjectResponse>>> getProjectsByUser() {
        List<GetProjectResponse> response = projectService.getProjectByUser();
        return ResponseEntity.ok(new BaseResponse<>("200", "Danh sách dự án", response));
    }

    @DeleteMapping(UrlConstant.PROJECT.DELETE_PROJECT)
    public ResponseEntity<BaseResponse<Boolean>> delete(@PathVariable UUID id) {
        boolean response = projectService.deleteProjectById(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa dự án thành công", response));
    }

    @PutMapping(UrlConstant.PROJECT.UPDATE_PROJECT)
    public ResponseEntity<BaseResponse<UpdateProjectResponse>> update(@PathVariable UUID id, @RequestBody UpdateProjectRequest request) {
        UpdateProjectResponse response = projectService.updateProjectById(id, request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Cập nhật dự án thành công", response));
    }

    @PostMapping(UrlConstant.PROJECT.INVITE_PROJECT)
    public ResponseEntity<BaseResponse<Boolean>> invite(@PathVariable UUID id,
                                                        @RequestParam String email,
                                                        @RequestParam String role) {
        boolean response = projectService.inviteUserToProject(id, email, role);
        return ResponseEntity.ok(new BaseResponse<>("200", "Thêm thành viên thành công", response));
    }

    @DeleteMapping(UrlConstant.PROJECT.DELETE_MEMBER)
    public ResponseEntity<BaseResponse<Boolean>> deleteMember(@PathVariable UUID id,
                                                              @RequestParam UUID userId) {
        boolean response = projectService.deleteUserFromProject(id, userId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa thành viên thành công", response));
    }

    @PostMapping(UrlConstant.PROJECT.CLOSE_PROJECT)
    public ResponseEntity<BaseResponse<Boolean>> closeProject(@PathVariable UUID id) {
        boolean response = projectService.closeProject(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Đóng dự án thành công", response));
    }
}
