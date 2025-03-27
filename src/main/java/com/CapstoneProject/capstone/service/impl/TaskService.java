package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueByTaskRequest;
import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueByTaskResponse;
import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.dto.response.task.CreateNewTaskResponse;
import com.CapstoneProject.capstone.dto.response.task.GetTaskResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.enums.*;
import com.CapstoneProject.capstone.exception.*;
import com.CapstoneProject.capstone.mapper.TaskMapper;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.ITaskService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import javax.imageio.plugins.bmp.BMPImageWriteParam;
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
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final IssueRepository issueRepository;

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
        ProjectMember pmMember = projectMemberRepository.findByProjectIdAndUserId(projectId, pmUser.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên"));
        ProjectMember projectMember = projectMemberRepository.findById(request.getAssigneeTo()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
//        ProjectMember isAlreadyMember = projectMemberRepository.findByProjectIdAndUserId(projectId, request.getAssigneeTo()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
        Role roleAssign = roleRepository.findById(projectMember.getRole().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò"));
        if(!roleAssign.getName().equals(RoleEnum.DEV)){
            throw new InvalidRoleException("Không phải role thực hiện task, thực hiện task là role DEV");
        }
        ProjectMember reporter = projectMemberRepository.findById(request.getReporter()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
        Role roleReporter = roleRepository.findById(reporter.getRole().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy vai trò"));
        if(!roleReporter.getName().equals(RoleEnum.QA)){
            throw new InvalidRoleException("Không phải role thực hiện report, reporter là role QA");
        }
        Task task = taskMapper.toModel(request);
        task.setTopic(topic);
        task.setAssignee(projectMember);
        task.setReporter(reporter);
        task.setCreatedBy(pmMember);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setActive(true);
        task.setStatus(StatusEnum.PENDING);
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
            User user = userRepository.findById(task.getCreatedBy().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

            GetUserResponse userResponse = userMapper.getUserResponse(user);

            UserProfile userProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            User assignee = userRepository.findById(task.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userAssigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile userProfileAssignee = userProfileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            User report = userRepository.findById(task.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userReporterResponse = userMapper.getUserResponse(report);
            UserProfile userProfileReporter = userProfileRepository.findByUserId(report.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
            GetProfileResponse userProfileAssigneeResponse = userProfileMapper.toProfile(userProfileAssignee);
            GetProfileResponse userProfileReporterResponse = userProfileMapper.toProfile(userProfileReporter);

            userAssigneeResponse.setProfile(userProfileAssigneeResponse);
            userResponse.setProfile(profileResponse);
            userReporterResponse.setProfile(userProfileReporterResponse);

            GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
            taskResponse.setUser(userResponse);
            taskResponse.setAssignee(userAssigneeResponse);
            taskResponse.setReporter(userReporterResponse);
            return taskResponse;
        }).collect(Collectors.toList());
        return responses;
    }

    @Override
    public GetTaskResponse getTask(UUID id, UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy task"));

        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));
        boolean isPM = (task.getCreatedBy() != null && task.getCreatedBy().getUser() != null)
                && task.getCreatedBy().getUser().getId().equals(userId);

        boolean isAssignee = (task.getAssignee() != null && task.getAssignee().getUser() != null)
                && task.getAssignee().getUser().getId().equals(userId);

        boolean isReporter = (task.getReporter() != null && task.getReporter().getUser() != null)
                && task.getReporter().getUser().getId().equals(userId);

        if (!isPM && !isAssignee && !isReporter) {
            throw new ForbiddenException("Bạn không có quyền xem task này");
        }

        User creator = userRepository.findById(task.getCreatedBy().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người tạo task"));
        GetUserResponse creatorResponse = userMapper.getUserResponse(creator);

        UserProfile creatorProfile = profileRepository.findByUserId(creator.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người tạo task"));
        creatorResponse.setProfile(userProfileMapper.toProfile(creatorProfile));

        GetUserResponse assigneeResponse = null;
        if (task.getAssignee() != null && task.getAssignee().getUser() != null) {
            User assignee = userRepository.findById(task.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao task"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao task"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }

        GetUserResponse reporterResponse = null;
        if (task.getReporter() != null && task.getReporter().getUser() != null) {
            User assignee = userRepository.findById(task.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được làm báo cáo"));

            reporterResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được làm báo cáo"));

            reporterResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }

        GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
        taskResponse.setUser(creatorResponse);
        taskResponse.setAssignee(assigneeResponse);
        taskResponse.setReporter(reporterResponse);

        return taskResponse;
    }


    @Override
    public Boolean deleteTask(UUID id, UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy task"));

        UUID userId = AuthenUtil.getCurrentUserId();

        Optional<User> pmUserOpt = userRepository.findUserWithRolePMByProjectId(projectId);
        if (pmUserOpt.isEmpty() || !pmUserOpt.get().getId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền xóa task này");
        }
        task.setActive(false);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return true;
    }

    @Override
    public GetTaskResponse updateTask(UUID id, UUID projectId, UUID topicId, String status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy task"));

        UUID userId = AuthenUtil.getCurrentUserId();

        Optional<User> pmUserOpt = userRepository.findUserWithRolePMByProjectId(projectId);

        boolean isPM = pmUserOpt.isPresent() && pmUserOpt.get().getId().equals(userId);

        boolean isAssignee = task.getAssignee().getUser().getId().equals(userId);


        if (!isPM && !isAssignee) {
            throw new ForbiddenException("Bạn không có quyền cập nhật task này");
        }

        StatusEnum newStatus;
        try {
            newStatus = StatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Trạng thái không hợp lệ! Chỉ chấp nhận OPEN, INPROGRESS, DONE.");
        }

        User creator = userRepository.findById(task.getCreatedBy().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người tạo task"));
        GetUserResponse creatorResponse = userMapper.getUserResponse(creator);

        UserProfile creatorProfile = profileRepository.findByUserId(creator.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người tạo task"));
        creatorResponse.setProfile(userProfileMapper.toProfile(creatorProfile));

        GetUserResponse assigneeResponse = null;
        if (task.getAssignee() != null && task.getAssignee().getUser() != null) {
            User assignee = userRepository.findById(task.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao task"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao task"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
        taskResponse.setUser(creatorResponse);
        taskResponse.setAssignee(assigneeResponse);
        return taskResponse;
    }

    @Override
    public CreateNewIssueByTaskResponse createNewIssueByTask(UUID id, UUID projectId, UUID topicId, CreateNewIssueByTaskRequest request) {
        String priorityStr = request.getPriority();
        PriorityEnum priority;
        try {
            priority = PriorityEnum.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Priority không hợp lệ! Chỉ chấp nhận LOW, MEDIUM, HIGH.");
        }

        String severityStr = request.getSeverity();
        SeverityEnum severity;
        try {
            severity = SeverityEnum.valueOf(severityStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Severity không hợp lệ! Chỉ chấp nhận MINOR, MODERATE, SIGNIFICANT, SEVERE, CATASTROPHIC.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy task"));
        if (!task.getTopic().getId().equals(topic.getId())) {
            throw new InvalidProjectException("Task không thuộc module đã chỉ định");
        }

        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        boolean isReporter = (task.getReporter() != null && task.getReporter().getUser() != null)
                && task.getReporter().getUser().getId().equals(userId);

        if (!isReporter) {
            throw new ForbiddenException("Bạn không có quyền tạo issue trong task đã chỉ định");
        }

        Issue issue = new Issue();
        issue.setLabel(request.getLabel());
        issue.setSummer(request.getSummer());
        issue.setDescription(request.getDescription());
        issue.setAttachment(request.getAttachment());
        issue.setStartDate(request.getStartDate());
        issue.setDueDate(request.getDueDate());
        issue.setPriority(priority);
        issue.setSeverity(severity);
        issue.setActive(true);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        issue.setAssignee(task.getAssignee());
        issue.setReporter(task.getReporter());
        issue.setCreatedBy(task.getReporter());
        issueRepository.save(issue);

        CreateNewIssueByTaskResponse response = new CreateNewIssueByTaskResponse();
        response.setId(issue.getId());
        response.setLabel(issue.getLabel());
        response.setSummer(issue.getSummer());
        response.setDescription(issue.getDescription());
        response.setAttachment(issue.getAttachment());
        response.setStartDate(issue.getStartDate());
        response.setDueDate(issue.getDueDate());
        response.setPriority(issue.getPriority().name());
        response.setSeverity(issue.getSeverity().name());
        return response;
    }

}
