package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.enums.GenderEnum;
import com.CapstoneProject.capstone.enums.PriorityEnum;
import com.CapstoneProject.capstone.enums.RoleEnum;
import com.CapstoneProject.capstone.enums.StatusEnum;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.mapper.TaskMapper;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TopicRepository topicRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileRepository profileRepository;

    @Override
    public CreateNewTaskResponse createNewTask(UUID topicId, CreateNewTaskRequest request) {
        String priorityStr = request.getPriority();
        PriorityEnum priority;
        try {
            priority = PriorityEnum.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Priority không hợp lệ! Chỉ chấp nhận LOW, MEDIUM, HIGH.");
        }
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        ProjectMember projectMember = projectMemberRepository.findProjectMember(request.getAssigneeTo()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên"));
        Task task = taskMapper.toModel(request);
        task.setTopic(topic);
        task.setAssignee(projectMember);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setActive(true);
        task.setStatus(StatusEnum.OPEN);
        taskRepository.save(task);
        User user = userRepository.findById(projectMember.getUser().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
        GetUserResponse userResponse = userMapper.getUserResponse(user);
        UserProfile userProfile = profileRepository.findByUserId(user.getId()).get();
        GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
        userResponse.setProfile(profileResponse);
        CreateNewTaskResponse createNewTaskResponse = taskMapper.toResponse(task);
        createNewTaskResponse.setUser(userResponse);
        return createNewTaskResponse;
    }
}
