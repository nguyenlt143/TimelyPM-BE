package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueByTaskRequest;
import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueByTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;

import java.util.List;
import java.util.UUID;

public interface ITaskService {
    CreateNewTaskResponse createNewTask(UUID projectId, UUID topicId, CreateNewTaskRequest request);
    List<GetTaskResponse> getTasks(UUID projectId, UUID topicId);
    GetTaskResponse getTask(UUID id, UUID projectId, UUID topicId);
    Boolean deleteTask(UUID id, UUID projectId, UUID topicId);
    GetTaskResponse updateTask(UUID id, UUID projectId, UUID topicId, String status);
    CreateNewIssueByTaskResponse createNewIssueByTask(UUID id, UUID projectId, UUID topicId, CreateNewIssueByTaskRequest request);
}
