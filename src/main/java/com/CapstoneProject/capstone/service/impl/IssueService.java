package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueRequest;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.enums.PriorityEnum;
import com.CapstoneProject.capstone.enums.StatusEnum;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.InvalidProjectException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.mapper.TaskMapper;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.IIssueService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueService implements IIssueService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TopicRepository topicRepository;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileRepository profileRepository;
    private final ProjectRepository projectRepository;

    @Override
    public CreateNewIssueResponse createNewIssue(UUID projectId, UUID topicId, CreateNewIssueRequest request) {
        String priorityStr = request.getPriority();
        PriorityEnum priority;
        try {
            priority = PriorityEnum.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Priority không hợp lệ! Chỉ chấp nhận LOW, MEDIUM, HIGH.");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();

        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Không tìm thấy Project Manager"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        ProjectMember projectMember = projectMemberRepository.findById(request.getAssigneeTo()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
        Issue issue = new Issue();
        issue.setLabel(request.getLabel());
        issue.setSummer(request.getSummer());
        issue.setDescription(request.getDescription());
        issue.setAttachment(request.getAttachment());
        issue.setAssignee(projectMember);
        issue.setStartDate(request.getStartDate());
        issue.setDueDate(request.getDueDate());
        issue.setStatus(StatusEnum.PENDING);
        issue.setPriority(priority);
        issue.setActive(true);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        issue.setTopic(topic);
        issueRepository.save(issue);
        User user = userRepository.findById(projectMember.getUser().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
        GetUserResponse userResponse = userMapper.getUserResponse(user);
        UserProfile userProfile = profileRepository.findByUserId(user.getId()).get();
        GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
        userResponse.setProfile(profileResponse);
        CreateNewIssueResponse response = new CreateNewIssueResponse();
        response.setId(issue.getId());
        response.setLabel(issue.getLabel());
        response.setSummer(issue.getSummer());
        response.setDescription(issue.getDescription());
        response.setAttachment(issue.getAttachment());
        response.setStartDate(issue.getStartDate());
        response.setDueDate(issue.getDueDate());
        response.setPriority(issue.getPriority().toString());
        response.setStatus(issue.getStatus().toString());
        response.setUser(userResponse);
        return response;
    }

    @Override
    public List<GetIssueResponse> getIssues(UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        List<Issue> issues = issueRepository.findByTopicId(topicId);

        List<GetIssueResponse> responses = new ArrayList<>();
        for(Issue issue : issues){
            GetIssueResponse response = new GetIssueResponse();
            response.setId(issue.getId());
            response.setLabel(issue.getLabel());
            response.setSummer(issue.getSummer());
            response.setDescription(issue.getDescription());
            response.setAttachment(issue.getAttachment());
            response.setStartDate(issue.getStartDate());
            response.setDueDate(issue.getDueDate());
            response.setPriority(issue.getPriority().toString());
            response.setStatus(issue.getStatus().toString());
            User user = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

            GetUserResponse userResponse = userMapper.getUserResponse(user);

            UserProfile userProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
            userResponse.setProfile(profileResponse);
            response.setUser(userResponse);
            responses.add(response);
        }
        return responses;
    }
}
