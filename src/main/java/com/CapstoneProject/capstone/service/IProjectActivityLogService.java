package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.projectActivityLog.GetProjectLogResponse;
import com.CapstoneProject.capstone.enums.ActivityTypeEnum;
import com.CapstoneProject.capstone.model.Project;
import com.CapstoneProject.capstone.model.User;

import java.util.List;
import java.util.UUID;

public interface IProjectActivityLogService {
    void logActivity(Project project, User user, ActivityTypeEnum activityType, String content);
    List<GetProjectLogResponse> getAllProjectLog(UUID projectId);
}
