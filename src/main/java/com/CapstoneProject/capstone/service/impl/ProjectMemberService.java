package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.IProjectMemberService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService implements IProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProjectRepository projectRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<GetProjectMemberResponse> getProjectMembers(UUID projectId) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của dự án"));

        List<GetProjectMemberResponse> projectMemberResponses = new ArrayList<>();
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        for (ProjectMember projectMember : projectMembers) {
            Optional<UserProfile> userProfile = userProfileRepository.findByUserId(projectMember.getUser().getId());
            Optional<Role> role = roleRepository.findById(projectMember.getRole().getId());
            GetProjectMemberResponse getProjectMemberResponse = new GetProjectMemberResponse();
            getProjectMemberResponse.setId(projectMember.getId());
            getProjectMemberResponse.setFullName(userProfile.get().getFullName());
            getProjectMemberResponse.setAvatarUrl(userProfile.get().getAvatarUrl());
            getProjectMemberResponse.setRole(role.get().getName().name());
            projectMemberResponses.add(getProjectMemberResponse);
        }
        return projectMemberResponses;
    }
}
