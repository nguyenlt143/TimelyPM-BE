package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.topic.CreateNewTopicRequest;
import com.CapstoneProject.capstone.dto.request.topic.UpdateTopicRequest;
import com.CapstoneProject.capstone.dto.response.topic.CreateNewTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.GetTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.UpdateTopicResponse;
import com.CapstoneProject.capstone.enums.ProjectStatusEnum;
import com.CapstoneProject.capstone.enums.StatusEnum;
import com.CapstoneProject.capstone.enums.TopicTypeEnum;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.exception.ProjectAlreadyCompletedException;
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
    @Override
    public CreateNewTopicResponse createNewTopic(CreateNewTopicRequest request) {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        if (project.getStatus().equals(StatusEnum.DONE.name())){
            throw new ProjectAlreadyCompletedException("Không thể thêm task mới vì dự án đã kết thúc.");
        }
        
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

        if (request.getStartDate().isAfter(request.getDueDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }

        Topic topic = topicMapper.toTopic(request);
        System.out.println("Before saving - Topic ID: " + topic.getId());
        System.out.println("Project ID: " + topic.getProject());
        topic.setActive(true);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());
        topicRepository.save(topic);
        return topicMapper.toResponse(topic);
    }

    @Override
    public List<GetTopicResponse> getTopics(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        UUID userId = AuthenUtil.getCurrentUserId();
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không có trong dự án này"));
        List<Topic> topics = topicRepository.findAll(projectMember.getProject().getId());
        return topics.stream().map(topicMapper::topicResponse).collect(Collectors.toList());
    }

    @Override
    public GetTopicResponse getTopic(UUID topicId, UUID projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        UUID userId = AuthenUtil.getCurrentUserId();
        ProjectMember pmUser = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không có trong dự án này"));
        Topic topic = topicRepository.getByIdTopic(topicId, projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy module này hoặc module không tìm thấy topic này"));

        GetTopicResponse response = topicMapper.topicResponse(topic);
        return response;
    }

    @Override
    public boolean deleteTopic(UUID topicId, UUID projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        UUID userId = AuthenUtil.getCurrentUserId();
        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }
        Topic topic = topicRepository.getByIdTopic(topicId, projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy module này hoặc module không tìm thấy topic này"));
        topic.setActive(false);
        topic.setUpdatedAt(LocalDateTime.now());
        topicRepository.save(topic);
        return true;
    }

    @Override
    public UpdateTopicResponse updateTopic(UUID topicId, UUID projectId, UpdateTopicRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này."));
        if (request.getType() != null){
            String typeStr = request.getType();
            try{
                TopicTypeEnum typeEnum = TopicTypeEnum.valueOf(typeStr.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }
        UUID userId = AuthenUtil.getCurrentUserId();
        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Bạn không có quyền hoặc không tồn tại"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }
        Topic topic = topicRepository.getByIdTopic(topicId, projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy module này hoặc module không tìm thấy topic này"));
        topic.setType(request.getType() == null ? topic.getType().toUpperCase() : request.getType().toUpperCase());
        topic.setDescription(request.getDescription() == null ? topic.getDescription() : request.getDescription());
        topic.setLabels(request.getLabels() == null ? topic.getLabels() : request.getLabels());
        if (request.getStartDate() != null) {
            topic.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            topic.setDueDate(request.getDueDate());
        }
        if (topic.getStartDate() != null && topic.getDueDate() != null) {
            if (topic.getStartDate().isAfter(topic.getDueDate())) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc.");
            }
        }
        topicRepository.save(topic);
        UpdateTopicResponse response = new UpdateTopicResponse();
        response.setType(topic.getType().toUpperCase());
        response.setDescription(topic.getDescription());
        response.setLabels(topic.getLabels());
        response.setStartDate(topic.getStartDate());
        response.setDueDate(topic.getDueDate());
        return response;
    }

}
