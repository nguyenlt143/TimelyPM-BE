package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.projectActivityLog.GetProjectLogResponse;
import com.CapstoneProject.capstone.enums.ActivityTypeEnum;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.Project;
import com.CapstoneProject.capstone.model.ProjectActivityLog;
import com.CapstoneProject.capstone.model.ProjectMember;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.repository.ProjectActivityLogRepository;
import com.CapstoneProject.capstone.repository.ProjectMemberRepository;
import com.CapstoneProject.capstone.repository.ProjectRepository;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectActivityLogService implements IProjectActivityLogService {
    private final ProjectActivityLogRepository projectActivityLogRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public void logActivity(Project project, User user, ActivityTypeEnum activityType, String content) {
        ProjectActivityLog log = new ProjectActivityLog();
        log.setActivityType(activityType);
        log.setProject(project);
        log.setContent(content);
        log.setCreateBy(user);
        log.setActive(true);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        projectActivityLogRepository.save(log);
    }

    @Override
    public List<GetProjectLogResponse> getAllProjectLog(UUID projectId) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        List<ProjectActivityLog> projectActivityLogs = projectActivityLogRepository.findAll(projectId);

        List<GetProjectLogResponse> responses = projectActivityLogs.stream().map(projectActivityLog -> {
            GetProjectLogResponse response = new GetProjectLogResponse();
            response.setActivityType(projectActivityLog.getActivityType());
            response.setEmail(projectActivityLog.getCreateBy().getEmail());
            response.setContent(projectActivityLog.getContent());
            response.setUpdateTime(projectActivityLog.getUpdatedAt());
            return response;
        }).collect(Collectors.toList());
        return responses;
    }


}
