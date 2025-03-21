package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;
import com.CapstoneProject.capstone.service.ITaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.TASK.TASK)
@RequiredArgsConstructor
public class TaskController {
    private final ITaskService taskService;

    @PostMapping(UrlConstant.TASK.CREATE)
    public ResponseEntity<BaseResponse<CreateNewTaskResponse>> create(@RequestParam UUID projectId, @RequestParam UUID topicId, @Valid @RequestBody CreateNewTaskRequest request) {
        CreateNewTaskResponse response = taskService.createNewTask(projectId, topicId, request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo topic thành công", response));
    }

    @GetMapping(UrlConstant.TASK.GET_TASKS)
    public ResponseEntity<BaseResponse<List<GetTaskResponse>>> GetAllTask(@RequestParam UUID projectId, @RequestParam UUID topicId) {
        List<GetTaskResponse> response = taskService.getTasks(projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo topic thành công", response));
    }
}
