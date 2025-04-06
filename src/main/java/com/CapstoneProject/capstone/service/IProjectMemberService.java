package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.projectMember.GetMemberPendingRespone;
import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.enums.MemberStatusEnum;

import java.util.List;
import java.util.UUID;

public interface IProjectMemberService {
    List<GetProjectMemberResponse> getProjectMembers(UUID projectId);
    boolean updateProjectMemberStatus(UUID projectId, UUID id, String role, MemberStatusEnum memberStatus);
    List<GetMemberPendingRespone> getMemberPending(UUID id);
}
