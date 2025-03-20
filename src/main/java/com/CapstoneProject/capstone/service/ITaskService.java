package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;

import java.util.UUID;

public interface ITaskService {
    CreateNewTaskResponse createNewTask(UUID topicId, CreateNewTaskRequest request);
}
