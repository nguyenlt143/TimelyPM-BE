package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.projectMember.GetMemberPendingRespone;
import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.enums.MemberStatusEnum;
import com.CapstoneProject.capstone.service.IProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.MEMBER.MEMBER)
@RequiredArgsConstructor
public class ProjectMemberController {
    private final IProjectMemberService projectMemberService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(UrlConstant.MEMBER.GET_MEMBERS)
    public ResponseEntity<BaseResponse<List<GetProjectMemberResponse>>> getProjectMembers(@PathVariable UUID projectId) {
        List<GetProjectMemberResponse> projectMemberResponses = projectMemberService.getProjectMembers(projectId);
        return ResponseEntity.ok(new BaseResponse<>("200", "List of ProjectMember", projectMemberResponses));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(UrlConstant.MEMBER.GET_MEMBERS_PENDING)
    public ResponseEntity<BaseResponse<List<GetMemberPendingRespone>>> getMemberPending(@PathVariable UUID projectId) {
        List<GetMemberPendingRespone> projectMemberResponses = projectMemberService.getMemberPending(projectId);
        return ResponseEntity.ok(new BaseResponse<>("200", "List of ProjectMember", projectMemberResponses));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping(UrlConstant.MEMBER.UPDATE_STATUS_MEMBER)
    public ResponseEntity<BaseResponse<Boolean>> updateStatusMember(@PathVariable UUID projectId,
                                                                   @RequestParam UUID memberId,
                                                                   @RequestParam String role,
                                                                   @RequestParam MemberStatusEnum status) {
        boolean projectMemberResponses = projectMemberService.updateProjectMemberStatus(projectId, memberId, role, status);
        return ResponseEntity.ok(new BaseResponse<>("200", "List of ProjectMember", projectMemberResponses));
    }
}
