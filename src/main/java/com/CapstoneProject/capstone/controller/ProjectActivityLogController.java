package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.projectActivityLog.GetProjectLogResponse;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.PROJECT_ACTIVITY_LOG.PROJECT_ACTIVITY)
public class ProjectActivityLogController {
    private final IProjectActivityLogService projectActivityLogService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(UrlConstant.PROJECT_ACTIVITY_LOG.GET_ALL)
    public ResponseEntity<BaseResponse<List<GetProjectLogResponse>>> getAllLog(@PathVariable UUID projectId) {
        List<GetProjectLogResponse> response = projectActivityLogService.getAllProjectLog(projectId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy danh sách log thành công", response));
    }
}
