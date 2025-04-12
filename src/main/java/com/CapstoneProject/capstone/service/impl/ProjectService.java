package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.project.CreateNewProjectRequest;
import com.CapstoneProject.capstone.dto.request.project.UpdateProjectRequest;
import com.CapstoneProject.capstone.dto.response.project.CreateNewProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.GetProjectResponse;
import com.CapstoneProject.capstone.dto.response.project.UpdateProjectResponse;
import com.CapstoneProject.capstone.dto.response.projectMember.GetMemberPendingRespone;
import com.CapstoneProject.capstone.dto.response.projectMember.GetProjectMemberResponse;
import com.CapstoneProject.capstone.enums.*;
import com.CapstoneProject.capstone.exception.*;
import com.CapstoneProject.capstone.mapper.ProjectMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import com.CapstoneProject.capstone.service.IProjectService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectActivityLogRepository projectActivityLogRepository;
    private final IProjectActivityLogService projectActivityLogService;

    @Override
    @Transactional
    public CreateNewProjectResponse createNewProject(CreateNewProjectRequest request) {
        UUID userID = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userID).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));
        Role role = roleRepository.findByName(RoleEnum.PM);
        String statusSt = request.getStatus();
        try{
            ProjectStatusEnum status = ProjectStatusEnum.valueOf(statusSt.toUpperCase());
        }catch (IllegalArgumentException | NullPointerException e){
            throw new InvalidEnumException("Trạng thái không hợp lệ");
        }

        Project project = projectMapper.toProject(request);
        project.setUserProfile(userProfile);

        project.setCode(UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 6));
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setActive(true);
        projectRepository.save(project);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.CREATE_PROJECT);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(user);
        projectMember.setRole(role);
        projectMember.setStatus(MemberStatusEnum.APPROVED);
        projectMember.setActive(true);
        projectMember.setCreatedAt(LocalDateTime.now());
        projectMember.setUpdatedAt(LocalDateTime.now());
        projectMemberRepository.save(projectMember);

        CreateNewProjectResponse response = projectMapper.toResponse(project);
        return response;
    }

    @Override
    public List<GetProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.getAllProjects();
        return projects.stream().map(GetProjectResponse::new).collect(Collectors.toList());
    }

    @Override
    public GetProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        GetProjectResponse response = projectMapper.toGetResponse(project);
        response.setUserId(project.getUserProfile().getUser().getId());
        return response;
    }

    @Override
    public boolean deleteProjectById(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        project.setActive(false);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.DELETE_PROJECT);

        return true;
    }

    @Override
    public UpdateProjectResponse updateProjectById(UUID id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        if (request.getStatus() != null){
            String statusSt = request.getStatus();
            try{
                ProjectStatusEnum status = ProjectStatusEnum.valueOf(statusSt.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }
        project.setName(request.getName() == null ? project.getName() : request.getName());
        project.setImage(request.getImage() == null ? project.getImage() : request.getImage());
        project.setStatus(request.getStatus() == null ? project.getStatus() : request.getStatus());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.UPDATE_PROJECT);

        UpdateProjectResponse response = projectMapper.toUpdateResponse(project);
        return response;
    }

    @Override
    public boolean inviteUserToProject(UUID projectId, String email, String role) {
        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Vai trò không hợp lệ, chỉ được QA, DEV");
        }

        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        if (project.getStatus().equals(StatusEnum.DONE.name())){
            throw new ProjectAlreadyCompletedException("Không thể thêm task mới vì dự án đã kết thúc.");
        }

        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        User userInvite = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        Optional<ProjectMember> isAlreadyMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userInvite.getId());
        if (isAlreadyMember.isPresent()){
            throw new UserExisted("Người dùng đã là thành viên dự án");
        }

        Role roleUser = roleRepository.findByName(roleEnum);
        ProjectMember projectMember = new ProjectMember();
        projectMember.setUser(userInvite);
        projectMember.setRole(roleUser);
        projectMember.setProject(project);
        projectMember.setStatus(MemberStatusEnum.APPROVED);
        projectMember.setCreatedAt(LocalDateTime.now());
        projectMember.setUpdatedAt(LocalDateTime.now());
        projectMember.setActive(true);
        projectMemberRepository.save(projectMember);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.MEMBER_JOIN);

        return true;
    }

    @Override
    public boolean deleteUserFromProject(UUID projectId, UUID id) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        ProjectMember isAlreadyMember = projectMemberRepository.findByProjectIdAndMemberId(project.getId(), id, MemberStatusEnum.APPROVED.name()).orElseThrow(()-> new NotFoundException("Không tìm thấy thành viên này trong dự án"));

        projectMemberRepository.delete(isAlreadyMember);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.REMOVE_MEMBER);

        return true;
    }

    @Override
    public List<GetProjectResponse> getProjectByUser() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        List<Project> projects = projectRepository.findByUser(userId);
        List<GetProjectResponse> projectResponses = projects.stream().map(GetProjectResponse::new).collect(Collectors.toList());
        return projectResponses;
    }

    @Override
    public boolean closeProject(UUID id) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        User pmUser = userRepository.findUserWithRolePMByProjectId(id).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        project.setStatus(ProjectStatusEnum.DONE.name());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.CHANGE_STATUS);

        return true;
    }

    @Override
    public boolean joinProject(String code) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        Project project = projectRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án"));

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(user);
        projectMember.setStatus(MemberStatusEnum.PENDING);
        projectMember.setActive(true);
        projectMember.setCreatedAt(LocalDateTime.now());
        projectMember.setUpdatedAt(LocalDateTime.now());
        projectMemberRepository.save(projectMember);

        return true;
    }

    @Override
    public boolean processingProject(UUID id) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        User pmUser = userRepository.findUserWithRolePMByProjectId(id).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        project.setStatus(ProjectStatusEnum.PROCESSING.name());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        projectActivityLogService.logActivity(project, ActivityTypeEnum.CHANGE_STATUS);

        return true;
    }

    @Override
    public List<GetProjectResponse> getAllProjectsByUserId() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        List<Project> projects = projectRepository.getApprovedProjectsByUserId(userId);

        return projects.stream().map(GetProjectResponse::new).collect(Collectors.toList());
    }

}
