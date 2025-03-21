package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;

import java.util.List;
import java.util.UUID;

public interface IProjectMemberService {
    List<GetProjectMemberResponse> getProjectMembers(UUID projectId);
}
