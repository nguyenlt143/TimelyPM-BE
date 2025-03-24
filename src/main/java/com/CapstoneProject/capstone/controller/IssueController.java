package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.service.IIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.ISSUE.ISSUE)
public class IssueController {
    private final IIssueService issueService;

    @PostMapping(UrlConstant.ISSUE.CREATE)
    public ResponseEntity<BaseResponse<CreateNewIssueResponse>> create(@RequestParam UUID projectId, @RequestParam UUID topicId, @Valid @RequestBody CreateNewIssueRequest request) {
        CreateNewIssueResponse response = issueService.createNewIssue(projectId, topicId, request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo issue thành công", response));
    }

    @GetMapping(UrlConstant.ISSUE.GET_ISSUES)
    public ResponseEntity<BaseResponse<List<GetIssueResponse>>> GetAllIssue(@RequestParam UUID projectId, @RequestParam UUID topicId) {
        List<GetIssueResponse> response = issueService.getIssues(projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy danh sách issue thành công", response));
    }
}
