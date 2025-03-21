package com.CapstoneProject.capstone.mapper;

import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;
import com.CapstoneProject.capstone.model.Task;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskMapper {
    ModelMapper modelMapper;
    public Task toModel(CreateNewTaskRequest request) {
        return modelMapper.map(request, Task.class);
    }
    public CreateNewTaskResponse toResponse(Task task) {
        return modelMapper.map(task, CreateNewTaskResponse.class);
    }
    public GetTaskResponse toGetResponse(Task task) {
        return modelMapper.map(task, GetTaskResponse.class);
    }
}
