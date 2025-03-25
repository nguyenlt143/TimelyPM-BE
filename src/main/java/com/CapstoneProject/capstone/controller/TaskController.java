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
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo task thành công", response));
    }

    @GetMapping(UrlConstant.TASK.GET_TASKS)
    public ResponseEntity<BaseResponse<List<GetTaskResponse>>> GetAllTask(@RequestParam UUID projectId, @RequestParam UUID topicId) {
        List<GetTaskResponse> response = taskService.getTasks(projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy danh sách task thành công", response));
    }

    @GetMapping(UrlConstant.TASK.GET_TASK)
    public ResponseEntity<BaseResponse<GetTaskResponse>> GetTask(@PathVariable UUID id,@RequestParam UUID projectId, @RequestParam UUID topicId) {
        GetTaskResponse response = taskService.getTask(id, projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy task thành công", response));
    }

    @GetMapping(UrlConstant.TASK.UPDATE_TASKS)
    public ResponseEntity<BaseResponse<GetTaskResponse>> UpdateTask(@PathVariable UUID id, @RequestParam UUID projectId, @RequestParam UUID topicId, @RequestParam String status) {
        GetTaskResponse response = taskService.updateTask(id, projectId, topicId, status);
        return ResponseEntity.ok(new BaseResponse<>("200", "Update task thành công", response));
    }

    @GetMapping(UrlConstant.TASK.DELETE_TASKS)
    public ResponseEntity<BaseResponse<Boolean>> deleteTask(@PathVariable UUID id, @RequestParam UUID projectId, @RequestParam UUID topicId) {
        boolean response = taskService.deleteTask(id, projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa task thành công", response));
    }
}
