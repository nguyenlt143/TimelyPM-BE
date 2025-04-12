package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.config.CommentWebSocketHandler;
import com.CapstoneProject.capstone.dto.request.comment.CreateCommentRequest;
import com.CapstoneProject.capstone.dto.response.comment.CreateCommentResponse;
import com.CapstoneProject.capstone.dto.response.comment.GetCommentResponse;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.Comment;
import com.CapstoneProject.capstone.model.Question;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.model.UserProfile;
import com.CapstoneProject.capstone.repository.CommentRepository;
import com.CapstoneProject.capstone.repository.QuestionRepository;
import com.CapstoneProject.capstone.repository.UserProfileRepository;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.ICommentService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {
    private final QuestionRepository questionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
//    private final CommentWebSocketHandler webSocketHandler;

    @Override
    public CreateCommentResponse createComment(UUID questionId, CreateCommentRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("User profile not found"));

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new NotFoundException("Question not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setQuestion(question);
        comment.setActive(true);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        CreateCommentResponse response = new CreateCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(userId);
        response.setAvatarUrl(userProfile.getAvatarUrl());
        response.setFullName(userProfile.getFullName());
        response.setQuestionId(questionId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAtString = comment.getCreatedAt().format(formatter);
        response.setCreatedAt(createdAtString);
//        try {
//            webSocketHandler.broadcastComment(response);
//        } catch (IOException e) {
//            System.err.println("Error broadcasting comment: " + e.getMessage());
//        }
        return response;
    }

    @Override
    public List<GetCommentResponse> getAllComments(UUID questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new NotFoundException("Question not found"));
        List<Comment> comments = commentRepository.findByQuestionId(question.getId());

        List<GetCommentResponse> responses = new ArrayList<>();
        for (Comment comment : comments){
            UserProfile userProfile = userProfileRepository.findByUserId(comment.getUser().getId()).orElseThrow(() -> new NotFoundException("User profile not found"));
            GetCommentResponse response = new GetCommentResponse();
            response.setId(comment.getId());
            response.setContent(comment.getContent());
            response.setUserId(comment.getUser().getId());
            response.setAvatarUrl(userProfile.getAvatarUrl());
            response.setFullName(userProfile.getFullName());
            response.setQuestionId(question.getId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAtString = comment.getCreatedAt().format(formatter);
            response.setCreatedAt(createdAtString);
            responses.add(response);
        }
        return responses;
    }
}
