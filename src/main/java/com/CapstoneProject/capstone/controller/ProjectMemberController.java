package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.service.IProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.MEMBER.MEMBER)
@RequiredArgsConstructor
public class ProjectMemberController {
    private final IProjectMemberService projectMemberService;

    @GetMapping(UrlConstant.MEMBER.GET_MEMBERS)
    public ResponseEntity<BaseResponse<List<GetProjectMemberResponse>>> getProjectMembers(UUID projectId) {
        List<GetProjectMemberResponse> projectMemberResponses = projectMemberService.getProjectMembers(projectId);
        return ResponseEntity.ok(new BaseResponse<>("200", "List of ProjectMember", projectMemberResponses));
    }
}
