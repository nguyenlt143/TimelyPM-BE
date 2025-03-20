package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;

public interface ITaskService {
    CreateNewTaskResponse createNewTask(CreateNewTaskRequest request);
}
