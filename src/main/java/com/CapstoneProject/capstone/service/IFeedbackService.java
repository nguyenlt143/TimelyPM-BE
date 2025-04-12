package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.feedback.CreateFeedbackRequest;
import com.CapstoneProject.capstone.dto.response.feedback.CreateFeedbackResponse;
import com.CapstoneProject.capstone.dto.response.feedback.GetFeedbackResponse;

import java.util.List;
import java.util.UUID;

public interface IFeedbackService {
    CreateFeedbackResponse createFeedback(CreateFeedbackRequest request);
    List<GetFeedbackResponse> getFeedbacks();
    GetFeedbackResponse getFeedback(UUID id);
    boolean deleteFeedback(UUID id);
}
