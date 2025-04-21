package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.projectMember.GetMemberPendingRespone;
import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.enums.ActivityTypeEnum;
import com.CapstoneProject.capstone.enums.MemberStatusEnum;
import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.exception.UserExisted;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import com.CapstoneProject.capstone.service.IProjectMemberService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final UserRepository userRepository;
    private final IProjectActivityLogService projectActivityLogService;
    private final UserProfileRepository profileRepository;

    @Override
    public List<GetProjectMemberResponse> getProjectMembers(UUID projectId) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của dự án"));

        List<GetProjectMemberResponse> projectMemberResponses = new ArrayList<>();
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId, MemberStatusEnum.APPROVED.name());
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

    @Override
    public boolean updateProjectMemberStatus(UUID projectId, UUID id, String role, MemberStatusEnum memberStatus) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        Optional<ProjectMember> isAlreadyMember = projectMemberRepository.findByProjectIdAndMemberId(projectId, id, MemberStatusEnum.APPROVED.name());
        if(isAlreadyMember.isPresent()){
            throw new UserExisted("Người dùng đã là thành viên dự án");
        }

        if (memberStatus == MemberStatusEnum.APPROVED) {
            RoleEnum roleEnum;
            try {
                roleEnum = RoleEnum.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new InvalidEnumException("Vai trò không hợp lệ, chỉ được QA, DEV");
            }
            Role roleUser = roleRepository.findByName(roleEnum);
            ProjectMember projectmember = projectMemberRepository.findByProjectIdAndMemberId(projectId, id, MemberStatusEnum.PENDING.name()).orElseThrow(() -> new NotFoundException("Không tìm thấy đơn của người này"));
            projectmember.setStatus(memberStatus);
            projectmember.setRole(roleUser);
            projectmember.setUpdatedAt(LocalDateTime.now());

            projectMemberRepository.save(projectmember);

            UserProfile profile = profileRepository.findByUserId(projectmember.getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

            projectActivityLogService.logActivity(project, pmUser, ActivityTypeEnum.ACCEPT_MEMBER, "Accepted " + profile.getFullName());

            return true;
        }else{
            ProjectMember projectmember = projectMemberRepository.findByProjectIdAndMemberId(projectId, id, MemberStatusEnum.PENDING.name()).orElseThrow(() -> new NotFoundException("Không tìm thấy đơn của người này"));
            projectmember.setStatus(memberStatus);
            projectmember.setUpdatedAt(LocalDateTime.now());
            projectMemberRepository.save(projectmember);
            return true;
        }

    }

    @Override
    public List<GetMemberPendingRespone> getMemberPending(UUID id) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        User pmUser = userRepository.findUserWithRolePMByProjectId(id).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        List<GetMemberPendingRespone> projectMemberResponses = new ArrayList<>();
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(id, MemberStatusEnum.PENDING.name());
        for (ProjectMember projectMember : projectMembers) {
            Optional<UserProfile> userProfile = userProfileRepository.findByUserId(projectMember.getUser().getId());
            GetMemberPendingRespone getProjectMemberResponse = new GetMemberPendingRespone();
            getProjectMemberResponse.setId(projectMember.getId());
            getProjectMemberResponse.setFullName(userProfile.get().getFullName());
            getProjectMemberResponse.setAvatarUrl(userProfile.get().getAvatarUrl());
            projectMemberResponses.add(getProjectMemberResponse);
        }
        return projectMemberResponses;
    }
}
