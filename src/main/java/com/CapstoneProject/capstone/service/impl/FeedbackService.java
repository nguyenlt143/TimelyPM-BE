package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.feedback.CreateFeedbackRequest;
import com.CapstoneProject.capstone.dto.response.feedback.CreateFeedbackResponse;
import com.CapstoneProject.capstone.dto.response.feedback.GetFeedbackResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.Feedback;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.model.UserProfile;
import com.CapstoneProject.capstone.repository.FeedbackRepository;
import com.CapstoneProject.capstone.repository.UserProfileRepository;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.IFeedbackService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    public CreateFeedbackResponse createFeedback(CreateFeedbackRequest request) {
        UUID userID = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userID).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setFeedback(request.getFeedback());
        feedback.setActive(true);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        CreateFeedbackResponse response = new CreateFeedbackResponse();
        response.setId(feedback.getId());
        response.setFeedback(feedback.getFeedback());

        GetUserResponse userResponse = userMapper.getUserResponse(user);
        UserProfile userProfile = userProfileRepository.findByUserId(userID).orElseThrow(() -> new NotFoundException("Profile not found!"));
        userResponse.setProfile(userProfileMapper.toProfile(userProfile));

        response.setUser(userResponse);
        return response;
    }

    @Override
    public List<GetFeedbackResponse> getFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        List<GetFeedbackResponse> responses = new ArrayList<>();
        for (Feedback feedback : feedbacks) {
            GetFeedbackResponse response = new GetFeedbackResponse();
            response.setId(feedback.getId());
            response.setFeedback(feedback.getFeedback());

            GetUserResponse userResponse = userMapper.getUserResponse(feedback.getUser());
            UserProfile userProfile = userProfileRepository.findByUserId(userResponse.getId()).orElseThrow(() -> new NotFoundException("Profile not found!"));
            userResponse.setProfile(userProfileMapper.toProfile(userProfile));

            response.setUser(userResponse);
            responses.add(response);
        }
        return responses;
    }

    @Override
    public GetFeedbackResponse getFeedback(UUID id) {
        Feedback feedback = feedbackRepository.findById(id).orElseThrow(() -> new NotFoundException("Feedback not found!"));
        GetFeedbackResponse response = new GetFeedbackResponse();
        response.setId(feedback.getId());
        response.setFeedback(feedback.getFeedback());

        GetUserResponse userResponse = userMapper.getUserResponse(feedback.getUser());
        UserProfile userProfile = userProfileRepository.findByUserId(userResponse.getId()).orElseThrow(() -> new NotFoundException("Profile not found!"));
        userResponse.setProfile(userProfileMapper.toProfile(userProfile));

        response.setUser(userResponse);
        return response;
    }

    @Override
    public boolean deleteFeedback(UUID id) {
        Feedback feedback = feedbackRepository.findById(id).orElseThrow(() -> new NotFoundException("Feedback not found!"));
        feedback.setActive(false);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
        return true;
    }
}
