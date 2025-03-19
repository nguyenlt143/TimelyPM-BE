package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.topic.CreateNewTopicRequest;
import com.CapstoneProject.capstone.dto.response.topic.CreateNewTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.GetTopicResponse;
import com.CapstoneProject.capstone.enums.TopicTypeEnum;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.mapper.TopicMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.ITopicService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService implements ITopicService {
    private final TopicMapper topicMapper;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final IssueRepository issueRepository;
    private final QuestionRepository questionRepository;
    @Override
    public CreateNewTopicResponse createNewTopic(CreateNewTopicRequest request) {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));

        UUID userId = AuthenUtil.getCurrentUserId();

        User pmUser = userRepository.findUserWithRolePMByProjectId(request.getProjectId()).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        TopicTypeEnum topicTypeEnum;
        try {
            topicTypeEnum = TopicTypeEnum.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Loại topic không hợp lệ! Chỉ chấp nhận TASK, ISSUE, QUESTION");
        }

        Topic topic = topicMapper.toTopic(request);
        topic.setActive(true);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());
        topicRepository.save(topic);
        if (topicTypeEnum.name().equals("TASK")){
            Task newTask = new Task();
            newTask.setTopic(topic);
            newTask.setCreatedAt(LocalDateTime.now());
            newTask.setUpdatedAt(LocalDateTime.now());
            newTask.setActive(true);
            taskRepository.save(newTask);
        }

        if (topicTypeEnum.name().equals("ISSUE")){
            Issue newIssue = new Issue();
            newIssue.setTopic(topic);
            newIssue.setCreatedAt(LocalDateTime.now());
            newIssue.setUpdatedAt(LocalDateTime.now());
            newIssue.setActive(true);
            issueRepository.save(newIssue);
        }

        if (topicTypeEnum.name().equals("QUESTION")){
            Question newQuestion = new Question();
            newQuestion.setTopic(topic);
            newQuestion.setCreatedAt(LocalDateTime.now());
            newQuestion.setUpdatedAt(LocalDateTime.now());
            newQuestion.setActive(true);
            questionRepository.save(newQuestion);
        }
        CreateNewTopicResponse createNewTopicResponse = topicMapper.toResponse(topic);
        return createNewTopicResponse;
    }

    @Override
    public List<GetTopicResponse> getTopics(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        UUID userId = AuthenUtil.getCurrentUserId();
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không có trong dự án này"));
        List<Topic> topics = topicRepository.findAll(projectMember.getProject().getId());
        return topics.stream().map(topicMapper::topicResponse).collect(Collectors.toList());
    }

}
