package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.feedback.CreateFeedbackRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.feedback.CreateFeedbackResponse;
import com.CapstoneProject.capstone.dto.response.feedback.GetFeedbackResponse;
import com.CapstoneProject.capstone.service.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.FEEDBACK.FEEDBACK)
@RequiredArgsConstructor
public class FeedbackController {
    private final IFeedbackService feedbackService;

    @PostMapping(UrlConstant.FEEDBACK.CREATE)
    public ResponseEntity<BaseResponse<CreateFeedbackResponse>> create(@RequestBody CreateFeedbackRequest request) {
        CreateFeedbackResponse response = feedbackService.createFeedback(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo feedback thành công", response));
    }

    @GetMapping(UrlConstant.FEEDBACK.GET_ALL_FEEDBACK)
    public ResponseEntity<BaseResponse<List<GetFeedbackResponse>>> getAll() {
        List<GetFeedbackResponse> response = feedbackService.getFeedbacks();
        return ResponseEntity.ok(new BaseResponse<>("200", "List feedback thành công", response));
    }

    @GetMapping(UrlConstant.FEEDBACK.GET_FEEDBACK)
    public ResponseEntity<BaseResponse<GetFeedbackResponse>> getFeedback(@PathVariable UUID id) {
        GetFeedbackResponse response = feedbackService.getFeedback(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Get feedback thành công", response));
    }

    @DeleteMapping(UrlConstant.FEEDBACK.DELETE_FEEDBACK)
    public ResponseEntity<BaseResponse<Boolean>> deleteFeedback(@PathVariable UUID id) {
        boolean response = feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa feedback thành công", response));
    }
}
