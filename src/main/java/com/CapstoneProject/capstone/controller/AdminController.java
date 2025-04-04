package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.ADMIN.ADMIN)
public class AdminController {
    private final IProjectService projectService;
    @GetMapping(UrlConstant.ADMIN.GET_PROJECTS)
    public ResponseEntity<BaseResponse<List<GetProjectResponse>>> getAllProject() {
        List<GetProjectResponse> response = projectService.getAllProjects();
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy danh sách issue thành công", response));
    }
}
