package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.question.CreateNewQuestionRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.question.CreateNewQuestionResponse;
import com.CapstoneProject.capstone.dto.response.question.GetQuestionResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;
import com.CapstoneProject.capstone.service.IQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.QUESTION.QUESTION)
public class QuestionController {
    private final IQuestionService questionService;

    @PostMapping(UrlConstant.QUESTION.CREATE)
    public ResponseEntity<BaseResponse<CreateNewQuestionResponse>> create(@RequestParam UUID projectId, @RequestParam UUID topicId, @Valid @RequestBody CreateNewQuestionRequest request) {
        CreateNewQuestionResponse response = questionService.createNewQuestion(projectId, topicId, request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo question thành công", response));
    }

    @GetMapping(UrlConstant.QUESTION.GET_QUESTIONS)
    public ResponseEntity<BaseResponse<List<GetQuestionResponse>>> GetAllQuestion(@RequestParam UUID projectId, @RequestParam UUID topicId) {
        List<GetQuestionResponse> response = questionService.getQuestions(projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy danh sách question thành công", response));
    }

    @GetMapping(UrlConstant.QUESTION.GET_QUESTION)
    public ResponseEntity<BaseResponse<GetQuestionResponse>> GetQuestion(@PathVariable UUID id, @RequestParam UUID projectId, @RequestParam UUID topicId) {
        GetQuestionResponse response = questionService.getQuestion(id, projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy question thành công", response));
    }
}
