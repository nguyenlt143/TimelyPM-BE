package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.question.CreateNewQuestionRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.question.CreateNewQuestionResponse;
import com.CapstoneProject.capstone.dto.response.question.GetQuestionResponse;
import com.CapstoneProject.capstone.service.IQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.QUESTION.QUESTION)
public class QuestionController {
    private final IQuestionService questionService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(UrlConstant.QUESTION.CREATE)
    public ResponseEntity<BaseResponse<CreateNewQuestionResponse>> create(@RequestParam UUID projectId,
                                                                          @RequestParam UUID topicId,
                                                                          @Valid @RequestBody CreateNewQuestionRequest request) {
        CreateNewQuestionResponse response = questionService.createNewQuestion(projectId, topicId, request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo question thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(UrlConstant.QUESTION.GET_QUESTIONS)
    public ResponseEntity<BaseResponse<List<GetQuestionResponse>>> GetAllQuestion(@RequestParam UUID projectId,
                                                                                  @RequestParam UUID topicId) {
        List<GetQuestionResponse> response = questionService.getQuestions(projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy danh sách question thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(UrlConstant.QUESTION.GET_QUESTION)
    public ResponseEntity<BaseResponse<GetQuestionResponse>> GetQuestion(@PathVariable UUID id,
                                                                         @RequestParam UUID projectId,
                                                                         @RequestParam UUID topicId) {
        GetQuestionResponse response = questionService.getQuestion(id, projectId, topicId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy question thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping(UrlConstant.QUESTION.UPDATE_QUESTION)
    public ResponseEntity<BaseResponse<GetQuestionResponse>> UpdateQuestion(@PathVariable UUID id,
                                                                            @RequestParam UUID projectId,
                                                                            @RequestParam UUID topicId,
                                                                            @RequestParam String status) {
        GetQuestionResponse response = questionService.updateQuestion(id, projectId, topicId, status);
        return ResponseEntity.ok(new BaseResponse<>("200", "Update question thành công", response));
    }
}
