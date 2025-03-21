package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.Project;
import com.CapstoneProject.capstone.model.ProjectMember;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.model.UserProfile;
import com.CapstoneProject.capstone.repository.ProjectMemberRepository;
import com.CapstoneProject.capstone.repository.ProjectRepository;
import com.CapstoneProject.capstone.repository.UserProfileRepository;
import com.CapstoneProject.capstone.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public List<GetProjectMemberResponse> getProjectMembers(UUID projectId) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }
        List<GetProjectMemberResponse> projectMemberResponses = new ArrayList<>();
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        for (ProjectMember projectMember : projectMembers) {
            Optional<UserProfile> userProfile = userProfileRepository.findByUserId(projectMember.getUser().getId());
            GetProjectMemberResponse getProjectMemberResponse = new GetProjectMemberResponse();
            getProjectMemberResponse.setId(projectMember.getId());
            getProjectMemberResponse.setFullName(userProfile.get().getFullName());
            getProjectMemberResponse.setAvatarUrl(userProfile.get().getAvatarUrl());
            projectMemberResponses.add(getProjectMemberResponse);
        }
        return projectMemberResponses;
    }
}
