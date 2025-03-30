package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueRequest;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;

import java.util.List;
import java.util.UUID;

public interface IIssueService {
    CreateNewIssueResponse createNewIssue(UUID projectId, UUID topicId, CreateNewIssueRequest request);
    List<GetIssueResponse> getIssues(UUID projectId, UUID topicId);
    GetIssueResponse getIssue(UUID id, UUID projectId, UUID topicId);
    GetIssueResponse getIssueByTask(UUID id, UUID projectId, UUID topicId, UUID taskId);
    GetIssueResponse updateIssue(UUID id, UUID projectId, UUID topicId, String status);
    Boolean deleteIssue(UUID id, UUID projectId, UUID topicId);
}
