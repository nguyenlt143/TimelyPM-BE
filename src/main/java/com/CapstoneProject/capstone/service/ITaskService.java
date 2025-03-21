package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;

import java.util.List;
import java.util.UUID;

public interface ITaskService {
    CreateNewTaskResponse createNewTask(UUID projectId, UUID topicId, CreateNewTaskRequest request);
    List<GetTaskResponse> getTasks(UUID projectId, UUID topicId);

}
