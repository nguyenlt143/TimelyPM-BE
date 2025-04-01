package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueRequest;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IIssueService {
    CreateNewIssueResponse createNewIssue(UUID projectId, UUID topicId, CreateNewIssueRequest request, MultipartFile file) throws IOException;
    List<GetIssueResponse> getIssues(UUID projectId, UUID topicId);
    GetIssueResponse getIssue(UUID id, UUID projectId, UUID topicId) throws IOException;
    GetIssueResponse getIssueByTask(UUID id, UUID projectId, UUID topicId, UUID taskId) throws IOException;
    GetIssueResponse updateIssue(UUID id, UUID projectId, UUID topicId, String status) throws IOException;
    Boolean deleteIssue(UUID id, UUID projectId, UUID topicId);
}
