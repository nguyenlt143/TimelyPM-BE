package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.enums.GenderEnum;
import com.CapstoneProject.capstone.enums.PriorityEnum;
import com.CapstoneProject.capstone.enums.RoleEnum;
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
import com.CapstoneProject.capstone.service.ITaskService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ProjectRepository projectRepository;

    @Override
    public CreateNewTaskResponse createNewTask(UUID projectId, UUID topicId, CreateNewTaskRequest request) {
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

        ProjectMember isAlreadyMember = projectMemberRepository.findByProjectIdAndUserId(projectId, request.getAssigneeTo()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));

        Task task = taskMapper.toModel(request);
        task.setTopic(topic);
        task.setAssignee(isAlreadyMember);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setActive(true);
        task.setStatus(StatusEnum.OPEN);
        taskRepository.save(task);
        User user = userRepository.findById(isAlreadyMember.getUser().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
        GetUserResponse userResponse = userMapper.getUserResponse(user);
        UserProfile userProfile = profileRepository.findByUserId(user.getId()).get();
        GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
        userResponse.setProfile(profileResponse);
        CreateNewTaskResponse createNewTaskResponse = taskMapper.toResponse(task);
        createNewTaskResponse.setUser(userResponse);
        return createNewTaskResponse;
    }

    @Override
    public List<GetTaskResponse> getTasks(UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        List<Task> tasks = taskRepository.findByTopicId(topicId);
        List<GetTaskResponse> responses = tasks.stream().map(task -> {
            User user = userRepository.findById(task.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

            GetUserResponse userResponse = userMapper.getUserResponse(user);

            UserProfile userProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
            userResponse.setProfile(profileResponse);

            GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
            taskResponse.setUser(userResponse);

            return taskResponse;
        }).collect(Collectors.toList());
        return responses;
    }
}
