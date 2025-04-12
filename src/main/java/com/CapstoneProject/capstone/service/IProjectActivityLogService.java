package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.projectActivityLog.GetProjectLogResponse;
import com.CapstoneProject.capstone.enums.ActivityTypeEnum;
import com.CapstoneProject.capstone.model.Project;

import java.util.List;
import java.util.UUID;

public interface IProjectActivityLogService {
    void logActivity(Project project, ActivityTypeEnum activityType);
    List<GetProjectLogResponse> getAllProjectLog(UUID projectId);
}
