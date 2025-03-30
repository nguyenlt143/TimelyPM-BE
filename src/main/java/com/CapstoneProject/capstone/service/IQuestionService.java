package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.question.CreateNewQuestionRequest;
import com.CapstoneProject.capstone.dto.response.question.CreateNewQuestionResponse;
import com.CapstoneProject.capstone.dto.response.question.GetQuestionResponse;

import java.util.List;
import java.util.UUID;

public interface IQuestionService {
    CreateNewQuestionResponse createNewQuestion(UUID projectId, UUID topicId, CreateNewQuestionRequest request);
    List<GetQuestionResponse> getQuestions(UUID projectId, UUID topicId);
    GetQuestionResponse getQuestion(UUID id, UUID projectId, UUID topicId);
}
