package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.comment.CreateCommentRequest;
import com.CapstoneProject.capstone.dto.response.comment.CreateCommentResponse;

import java.util.UUID;

public interface ICommentService {
    CreateCommentResponse createComment(UUID questionId, CreateCommentRequest request);
}
