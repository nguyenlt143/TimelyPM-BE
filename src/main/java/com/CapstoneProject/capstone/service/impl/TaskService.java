package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueByTaskRequest;
import com.CapstoneProject.capstone.dto.request.task.CreateNewTaskRequest;
import com.CapstoneProject.capstone.dto.request.task.UpdateTaskRequest;
import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueByTaskResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.notification.GetNotificationResponse;
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
import com.CapstoneProject.capstone.service.INotificationService;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import com.CapstoneProject.capstone.service.ITaskService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.services.drive.Drive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final GoogleDriveService googleDriveService;
    private final FileRepository fileRepository;
    private final IProjectActivityLogService projectActivityLogService;
    private final NotificationRepository notificationRepository;
    private final INotificationService notificationService;

    @Override
    public CreateNewTaskResponse createNewTask(UUID projectId, UUID topicId, CreateNewTaskRequest request, MultipartFile file) throws IOException {
        String priorityStr = request.getPriority();
        PriorityEnum priority;
        try {
            priority = PriorityEnum.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Priority không hợp lệ! Chỉ chấp nhận LOW, MEDIUM, HIGH.");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        if(project.getStatus().equals(StatusEnum.PENDING.name()) || project.getStatus().equals(StatusEnum.DONE.name())){
            throw new ProjectAlreadyCompletedException("Không thể thêm task mới vì dự án chưa bắt đầu hoặc đã kết thúc.");
        }
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

        Optional<String> maxTaskLabelOpt = taskRepository.findMaxTaskLabelByTopicId(topicId);
        int newTaskNumber = 1;
        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);
        if (maxTaskLabelOpt.isPresent()) {
            String maxTaskLabel = maxTaskLabelOpt.get();

            String[] parts = maxTaskLabel.split("-");
            String lastPart = parts[parts.length - 1];
            try {
                newTaskNumber = Integer.parseInt(lastPart);
                newTaskNumber++;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi khi chuyển đổi số: " + e.getMessage());
            }
        }
        String taskLabel = String.format("%s-%s-Task-%03d", project.getName(), topic.getLabels(), newTaskNumber);

        Task task = taskMapper.toModel(request);
        task.setLabel(taskLabel);
        task.setAttachment(url.getFileUrl());
        task.setPriority(priority);
        task.setTopic(topic);
        task.setAssignee(projectMember);
        task.setReporter(reporter);
        task.setCreatedBy(pmMember);
        task.setStartDate(Date.valueOf(request.getStartDate()));
        task.setDueDate(Date.valueOf(request.getDueDate()));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setActive(true);
        task.setStatus(TaskStatusEnum.PENDING);
        taskRepository.save(task);

        UserProfile pmProfile = profileRepository.findByUserId(pmUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));
        User assigneeUser = userRepository.findById(projectMember.getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng assignee"));
        User reporterUser = userRepository.findById(reporter.getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng reporter"));

        Notification assigneeNotification = new Notification();
        assigneeNotification.setMessage(String.format("Bạn được giao task mới '%s' bởi %s trong dự án %s",
                taskLabel, pmProfile.getFullName(), project.getName()));
        assigneeNotification.setRead(false);
        assigneeNotification.setUser(assigneeUser);
        assigneeNotification.setProject(project);
        assigneeNotification.setActive(true);
        assigneeNotification.setCreatedAt(LocalDateTime.now());
        assigneeNotification.setUpdatedAt(LocalDateTime.now());
        notificationService.createNotification(assigneeNotification);

        Notification reporterNotification = new Notification();
        reporterNotification.setMessage(String.format("Task mới '%s' được tạo bởi %s trong dự án %s, bạn là reporter",
                taskLabel, pmProfile.getFullName(), project.getName()));
        reporterNotification.setRead(false);
        reporterNotification.setUser(reporterUser);
        reporterNotification.setProject(project);
        reporterNotification.setActive(true);
        reporterNotification.setCreatedAt(LocalDateTime.now());
        reporterNotification.setUpdatedAt(LocalDateTime.now());
        notificationService.createNotification(reporterNotification);

        User user = userRepository.findById(projectMember.getUser().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
        GetUserResponse userResponse = userMapper.getUserResponse(user);
        UserProfile userProfile = profileRepository.findByUserId(user.getId()).get();
        GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);

        projectActivityLogService.logActivity(project, user, ActivityTypeEnum.CREATE_TASK, userProfile.getFullName() + " created new task successfully");

        userResponse.setProfile(profileResponse);
        CreateNewTaskResponse createNewTaskResponse = taskMapper.toResponse(task);
        createNewTaskResponse.setAttachment(url.getFileUrl());
        createNewTaskResponse.setAttachmentName(url.getFileName());
        createNewTaskResponse.setUser(userResponse);
        return createNewTaskResponse;
    }

    @Override
    public List<GetTaskResponse> getTasks(UUID projectId, UUID topicId) throws IOException {
        // Kiểm tra project và member
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        // Kiểm tra topic
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        // Lấy tasks
        List<Task> tasks = taskRepository.findByTopicId(topicId);

        // Thu thập userId từ Task
        Set<UUID> userIds = tasks.stream()
                .flatMap(task -> Stream.of(
                        task.getCreatedBy().getUser().getId(),
                        task.getAssignee().getUser().getId(),
                        task.getReporter().getUser().getId()))
                .collect(Collectors.toSet());

        // Lấy tất cả User và UserProfile theo lô
        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        Map<UUID, UserProfile> profileMap = profileRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile));

        // Lấy taskId
        List<UUID> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());

        // Lấy tất cả Issue theo lô
        List<Issue> issues = issueRepository.findByTaskIds(taskIds);
        Map<UUID, List<Issue>> issuesByTaskId = issues.stream()
                .collect(Collectors.groupingBy(issue -> issue.getTask().getId()));

        // Thu thập userId từ Issue
        issues.forEach(issue -> {
            userIds.add(issue.getAssignee().getUser().getId());
            userIds.add(issue.getReporter().getUser().getId());
            userIds.add(issue.getCreatedBy().getUser().getId());
        });

        // Lấy User và UserProfile bổ sung cho Issue
        userMap.putAll(userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user)));
        profileMap.putAll(profileRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile)));

        // Lấy tất cả File theo lô
        List<File> files = fileRepository.findByTaskIds(taskIds);
        Map<UUID, List<File>> filesByTaskId = files.stream()
                .collect(Collectors.groupingBy(file -> file.getTask().getId()));

        // Lấy Google Drive metadata theo lô
        Drive driveService;
        try {
            driveService = googleDriveService.getDriveService();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi khởi tạo Google Drive service", e);
        }
        BatchRequest batch = driveService.batch();
        Map<String, GoogleDriveResponse> driveResponseMap = new HashMap<>();

        for (Task task : tasks) {
            String fileId = googleDriveService.extractFileId(task.getAttachment());
            if (fileId != null) {
                JsonBatchCallback<com.google.api.services.drive.model.File> callback = new JsonBatchCallback<>() {
                    @Override
                    public void onSuccess(com.google.api.services.drive.model.File file, com.google.api.client.http.HttpHeaders httpHeaders) throws IOException {
                        GoogleDriveResponse response = new GoogleDriveResponse();
                        response.setFileName(file.getName());
                        response.setFileUrl(file.getWebViewLink());
                        response.setDownloadUrl(file.getWebContentLink());
                        driveResponseMap.put(fileId, response);
                    }

                    @Override
                    public void onFailure(GoogleJsonError googleJsonError, com.google.api.client.http.HttpHeaders httpHeaders) throws IOException {
                        throw new RuntimeException("Lỗi khi lấy tệp từ Google Drive: " + googleJsonError.getMessage());
                    }
                };
                driveService.files().get(fileId)
                        .setFields("name, webViewLink, webContentLink")
                        .queue(batch, callback);
            }
        }

        for (File file : files) {
            String fileId = googleDriveService.extractFileId(file.getUrl());
            if (fileId != null) {
                JsonBatchCallback<com.google.api.services.drive.model.File> callback = new JsonBatchCallback<>() {
                    @Override
                    public void onSuccess(com.google.api.services.drive.model.File file, com.google.api.client.http.HttpHeaders httpHeaders) throws IOException {
                        GoogleDriveResponse response = new GoogleDriveResponse();
                        response.setFileName(file.getName());
                        response.setFileUrl(file.getWebViewLink());
                        response.setDownloadUrl(file.getWebContentLink());
                        driveResponseMap.put(fileId, response);
                    }

                    @Override
                    public void onFailure(GoogleJsonError googleJsonError, com.google.api.client.http.HttpHeaders httpHeaders) throws IOException {
                        throw new RuntimeException("Lỗi khi lấy tệp từ Google Drive: " + googleJsonError.getMessage());
                    }
                };
                driveService.files().get(fileId)
                        .setFields("name, webViewLink, webContentLink")
                        .queue(batch, callback);
            }
        }

        try {
            batch.execute();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi thực thi batch request Google Drive", e);
        }

        // Ánh xạ tasks
        List<GetTaskResponse> responses = tasks.stream().map(task -> {
            GetTaskResponse taskResponse = taskMapper.toGetResponse(task);

            // Ánh xạ user
            User createdBy = userMap.get(task.getCreatedBy().getUser().getId());
            UserProfile createdByProfile = profileMap.get(createdBy.getId());
            GetUserResponse userResponse = userMapper.getUserResponse(createdBy);
            userResponse.setProfile(userProfileMapper.toProfile(createdByProfile));

            // Ánh xạ assignee
            User assignee = userMap.get(task.getAssignee().getUser().getId());
            UserProfile assigneeProfile = profileMap.get(assignee.getId());
            GetUserResponse assigneeResponse = userMapper.getUserResponse(assignee);
            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));

            // Ánh xạ reporter
            User reporter = userMap.get(task.getReporter().getUser().getId());
            UserProfile reporterProfile = profileMap.get(reporter.getId());
            GetUserResponse reporterResponse = userMapper.getUserResponse(reporter);
            reporterResponse.setProfile(userProfileMapper.toProfile(reporterProfile));

            // Ánh xạ issues
            List<Issue> taskIssues = issuesByTaskId.getOrDefault(task.getId(), Collections.emptyList());
            List<GetIssueResponse> issueResponses = taskIssues.stream().map(issue -> {
                GetIssueResponse issueResponse = new GetIssueResponse();
                issueResponse.setId(issue.getId());
                issueResponse.setLabel(issue.getLabel());
                issueResponse.setSummer(issue.getSummer());
                issueResponse.setDescription(issue.getDescription());
                issueResponse.setStartDate(issue.getStartDate());
                issueResponse.setDueDate(issue.getDueDate());
                issueResponse.setPriority(issue.getPriority().name());
                issueResponse.setStatus(issue.getStatus().name());
                issueResponse.setSeverity(issue.getSeverity().name());

                User issueAssignee = userMap.get(issue.getAssignee().getUser().getId());
                UserProfile issueAssigneeProfile = profileMap.get(issueAssignee.getId());
                GetUserResponse issueAssigneeResponse = userMapper.getUserResponse(issueAssignee);
                issueAssigneeResponse.setProfile(userProfileMapper.toProfile(issueAssigneeProfile));

                User issueReporter = userMap.get(issue.getReporter().getUser().getId());
                UserProfile issueReporterProfile = profileMap.get(issueReporter.getId());
                GetUserResponse issueReporterResponse = userMapper.getUserResponse(issueReporter);
                issueReporterResponse.setProfile(userProfileMapper.toProfile(issueReporterProfile));

                User issueCreatedBy = userMap.get(issue.getCreatedBy().getUser().getId());
                UserProfile issueCreatedByProfile = profileMap.get(issueCreatedBy.getId());
                GetUserResponse issueCreatedByResponse = userMapper.getUserResponse(issueCreatedBy);
                issueCreatedByResponse.setProfile(userProfileMapper.toProfile(issueCreatedByProfile));

                issueResponse.setAssignee(issueAssigneeResponse);
                issueResponse.setReporter(issueReporterResponse);
                issueResponse.setUser(issueCreatedByResponse);
                return issueResponse;
            }).collect(Collectors.toList());

            // Ánh xạ Google Drive
            GoogleDriveResponse attachmentResponse = driveResponseMap.get(googleDriveService.extractFileId(task.getAttachment()));
            List<GoogleDriveResponse> fileResponses = filesByTaskId.getOrDefault(task.getId(), Collections.emptyList()).stream()
                    .map(file -> driveResponseMap.get(googleDriveService.extractFileId(file.getUrl())))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            taskResponse.setUser(userResponse);
            taskResponse.setAssignee(assigneeResponse);
            taskResponse.setReporter(reporterResponse);
            taskResponse.setAttachment(attachmentResponse);
            taskResponse.setFileResponse(fileResponses);
            taskResponse.setIssues(issueResponses);
            return taskResponse;
        }).collect(Collectors.toList());

        return responses;
    }

    @Override
    public GetTaskResponse getTask(UUID id, UUID projectId, UUID topicId) throws IOException {
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
            User reporter = userRepository.findById(task.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được làm báo cáo"));

            reporterResponse = userMapper.getUserResponse(reporter);
            UserProfile reporterProfile = profileRepository.findByUserId(reporter.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được làm báo cáo"));

            reporterResponse.setProfile(userProfileMapper.toProfile(reporterProfile));
        }

        Drive driveService = googleDriveService.getDriveService();
        String fileId = googleDriveService.extractFileId(task.getAttachment());
        com.google.api.services.drive.model.File file = driveService.files().get(fileId).setFields("name, webViewLink, webContentLink").execute();
        GoogleDriveResponse googleDriveResponse = new GoogleDriveResponse();
        googleDriveResponse.setFileUrl(file.getWebViewLink());
        googleDriveResponse.setFileName(file.getName());
        googleDriveResponse.setDownloadUrl(file.getWebContentLink());

        List<Issue> issues = issueRepository.findAllByTaskId(task.getId());
        List<GetIssueResponse> issueResponses = new ArrayList<>();
        for (Issue issue : issues) {
            GetIssueResponse issueResponse = new GetIssueResponse();
            issueResponse.setId(issue.getId());
            issueResponse.setLabel(issue.getLabel());
            issueResponse.setSummer(issue.getSummer());
            issueResponse.setDescription(issue.getDescription());
            issueResponse.setStartDate(issue.getStartDate());
            issueResponse.setDueDate(issue.getDueDate());
            issueResponse.setPriority(issue.getPriority().name());
            issueResponse.setStatus(issue.getStatus().name());
            issueResponse.setSeverity(issue.getSeverity().name());

            User assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse assigneeResponseIssue = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = userProfileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));
            assigneeResponseIssue.setProfile(userProfileMapper.toProfile(assigneeProfile));

            User reporter = userRepository.findById(issue.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse reporterResponseIssue = userMapper.getUserResponse(reporter);
            UserProfile reporterProfile = userProfileRepository.findByUserId(reporter.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));
            reporterResponseIssue.setProfile(userProfileMapper.toProfile(reporterProfile));

            User createdBy = userRepository.findById(issue.getCreatedBy().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse createdByResponse = userMapper.getUserResponse(createdBy);
            UserProfile createdByProfile = userProfileRepository.findByUserId(createdBy.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));
            createdByResponse.setProfile(userProfileMapper.toProfile(createdByProfile));

            issueResponse.setAssignee(assigneeResponseIssue);
            issueResponse.setReporter(reporterResponseIssue);
            issueResponse.setUser(createdByResponse);
            issueResponses.add(issueResponse);
        }

        List<GoogleDriveResponse> googleDriveResponses = new ArrayList<>();
        List<File> files = fileRepository.findByTaskId(task.getId());
        for (File fileTask : files) {
            String fileTaskId = googleDriveService.extractFileId(fileTask.getUrl());
            try {
                com.google.api.services.drive.model.File driveFile = driveService.files()
                        .get(fileTaskId)
                        .setFields("name, webViewLink, webContentLink")
                        .execute();

                GoogleDriveResponse fileResponse = new GoogleDriveResponse();
                fileResponse.setFileName(driveFile.getName());
                fileResponse.setFileUrl(driveFile.getWebViewLink());
                fileResponse.setDownloadUrl(driveFile.getWebContentLink());

                googleDriveResponses.add(fileResponse);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lấy thông tin file từ Google Drive", e);
            }
        }
        GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
        taskResponse.setAttachment(googleDriveResponse);
        taskResponse.setUser(creatorResponse);
        taskResponse.setAssignee(assigneeResponse);
        taskResponse.setReporter(reporterResponse);
        taskResponse.setFileResponse(googleDriveResponses);

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
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        Optional<User> pmUserOpt = userRepository.findUserWithRolePMByProjectId(projectId);
        if (pmUserOpt.isEmpty() || !pmUserOpt.get().getId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền xóa task này");
        }

        UserProfile profile = profileRepository.findByUserId(pmUserOpt.get().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        task.setActive(false);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.DELETE_TASK, profileCurrentUser.getFullName() + " deleted " + task.getLabel());

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

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        Optional<User> pmUserOpt = userRepository.findUserWithRolePMByProjectId(projectId);

        boolean isPM = pmUserOpt.isPresent() && pmUserOpt.get().getId().equals(userId);
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getUser().getId().equals(userId);
        boolean isReporter = task.getReporter().getUser().getId().equals(userId);

        if (!isPM && !isAssignee && !isReporter) {
            throw new ForbiddenException("Bạn không có quyền cập nhật task này");
        }

        TaskStatusEnum newStatus;
        try {
            newStatus = TaskStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Trạng thái không hợp lệ! Các trạng thái hợp lệ: " +
                    Arrays.toString(TaskStatusEnum.values()));
        }

        TaskStatusEnum currentStatus = task.getStatus();
        boolean isValidTransition = false;
        List<TaskStatusEnum> assigneeStatuses = Arrays.asList(TaskStatusEnum.TODO, TaskStatusEnum.IN_PROGRESS, TaskStatusEnum.WAITING_TEST);

        if (assigneeStatuses.contains(newStatus) && !isAssignee) {
            throw new ForbiddenException("Chỉ assignee mới có thể chuyển trạng thái sang " + newStatus);
        }
        if (newStatus == TaskStatusEnum.DONE && !isReporter) {
            throw new ForbiddenException("Chỉ reporter mới có thể chuyển trạng thái sang DONE");
        }

        switch (currentStatus) {
            case PENDING:
                isValidTransition = newStatus == TaskStatusEnum.TODO;
                break;

            case TODO:
                isValidTransition = newStatus == TaskStatusEnum.IN_PROGRESS;
                break;

            case IN_PROGRESS:
                isValidTransition = newStatus == TaskStatusEnum.WAITING_TEST;
                break;

            case WAITING_TEST:
                isValidTransition = newStatus == TaskStatusEnum.DONE;
                if (isValidTransition) {
                    List<Issue> relatedIssues = issueRepository.findAllByTaskId(task.getId());
                    if (!relatedIssues.isEmpty()) {
                        for (Issue issue : relatedIssues) {
                            if (issue.getStatus() != IssueStatusEnum.CLOSED && issue.getStatus() != IssueStatusEnum.NOT_BUG) {
                                throw new InvalidEnumException("Không thể chuyển sang DONE: Issue " + issue.getLabel() +
                                        " chưa ở trạng thái DONE hoặc NOT_BUG");
                            }
                        }
                    }
                }
                break;

            case DONE:
                isValidTransition = false;
                break;
        }

        if (!isValidTransition) {
            throw new InvalidEnumException("Chuyển đổi trạng thái không hợp lệ! Từ " + currentStatus +
                    ", chỉ có thể chuyển sang: " + getAllowedTransitions(currentStatus));
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

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.UPDATE_TASK,
                profileCurrentUser.getFullName() + " updated status of " + task.getLabel() + " to " + newStatus);

        GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
        taskResponse.setUser(creatorResponse);
        taskResponse.setAssignee(assigneeResponse);
        return taskResponse;
    }

    private String getAllowedTransitions(TaskStatusEnum currentStatus) {
        switch (currentStatus) {
            case PENDING:
                return "[TODO]";
            case TODO:
                return "[IN_PROGRESS]";
            case IN_PROGRESS:
                return "[WAITING_TEST]";
            case WAITING_TEST:
                return "[DONE]";
            default:
                return "[]";
        }
    }

    @Override
    public CreateNewIssueByTaskResponse createNewIssueByTask(UUID id, UUID projectId, UUID topicId, CreateNewIssueByTaskRequest request, MultipartFile file) throws IOException {
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
            throw new InvalidEnumException("Severity không hợp lệ! Chỉ chấp nhận MINOR, MAJOR, CRITICAL.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        if(project.getStatus().equals(StatusEnum.PENDING.name()) || project.getStatus().equals(StatusEnum.DONE.name())){
            throw new ProjectAlreadyCompletedException("Không thể thêm issue mới vì dự án chưa bắt đầu hoặc đã kết thúc.");
        }

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

        if (task.getStatus() != TaskStatusEnum.WAITING_TEST) {
            throw new ForbiddenException("Chỉ có thể tạo issue khi task ở trạng thái WAITING_TEST.");
        }

        UUID userId = AuthenUtil.getCurrentUserId();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));


        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        boolean isReporter = (task.getReporter() != null && task.getReporter().getUser() != null)
                && task.getReporter().getUser().getId().equals(userId);

        if (!isReporter) {
            throw new ForbiddenException("Bạn không có quyền tạo issue trong task đã chỉ định");
        }

        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);

        Issue issue = new Issue();
        issue.setLabel(request.getLabel());
        issue.setSummer(request.getSummer());
        issue.setDescription(request.getDescription());
        issue.setAttachment(url.getFileUrl());
        issue.setStartDate(request.getStartDate());
        issue.setDueDate(request.getDueDate());
        issue.setStatus(IssueStatusEnum.OPEN);
        issue.setPriority(priority);
        issue.setSeverity(severity);
        issue.setTopic(topic);
        issue.setTask(task);
        issue.setActive(true);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        issue.setAssignee(task.getAssignee());
        issue.setReporter(task.getReporter());
        issue.setCreatedBy(task.getReporter());
        issueRepository.save(issue);


        User assigneeUser = userRepository.findById(issue.getAssignee().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng assignee"));
        User reporterUser = userRepository.findById(issue.getReporter().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng reporter"));
        UserProfile reporterProfile = profileRepository.findByUserId(reporterUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người báo cáo issue"));

        Notification assigneeNotification = new Notification();
        assigneeNotification.setMessage(String.format("Bạn được giao issue mới '%s' bởi %s trong dự án %s",
                issue.getLabel(), reporterProfile.getFullName(), project.getName()));
        assigneeNotification.setRead(false);
        assigneeNotification.setUser(assigneeUser);
        assigneeNotification.setProject(project);
        assigneeNotification.setActive(true);
        assigneeNotification.setCreatedAt(LocalDateTime.now());
        assigneeNotification.setUpdatedAt(LocalDateTime.now());
        notificationService.createNotification(assigneeNotification);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.CREATE_ISSUE, profileCurrentUser.getFullName() + " created " + issue.getLabel());

        User assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người nhận issue"));
        GetUserResponse assigneeResponse = userMapper.getUserResponse(assignee);
        UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người nhận issue"));
        assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));

        GetUserResponse reporterResponse = userMapper.getUserResponse(reporterUser);

        reporterResponse.setProfile(userProfileMapper.toProfile(reporterProfile));

        CreateNewIssueByTaskResponse response = new CreateNewIssueByTaskResponse();
        response.setId(issue.getId());
        response.setLabel(issue.getLabel());
        response.setSummer(issue.getSummer());
        response.setDescription(issue.getDescription());
        response.setAttachment(issue.getAttachment());
        response.setStartDate(issue.getStartDate());
        response.setAttachment(url.getFileUrl());
        response.setAttachmentName(url.getFileName());
        response.setDueDate(issue.getDueDate());
        response.setPriority(issue.getPriority().name());
        response.setSeverity(issue.getSeverity().name());
        response.setUser(reporterResponse);
        response.setAssignee(assigneeResponse);
        response.setReporter(reporterResponse);
        return response;
    }

    @Override
    public GetTaskResponse updateTask(UUID id, UUID projectId, UUID topicId, UpdateTaskRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Không tìm thấy Project Manager"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy task trong module này"));

        PriorityEnum priority = null;
        if (request.getPriority() != null){
            String prioritySt = request.getPriority();
            try{
                priority = PriorityEnum.valueOf(prioritySt.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }

        task.setSummer(request.getSummer() == null ? task.getSummer() : request.getSummer());
        task.setDescription(request.getDescription() == null ? task.getDescription() : request.getDescription());
        task.setStartDate(request.getStartDate() == null ? task.getStartDate() : request.getStartDate());
        task.setDueDate(request.getDueDate() == null ? task.getDueDate() : request.getDueDate());
        task.setPriority(request.getPriority() == null ? task.getPriority() : priority);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.UPDATE_TASK, profileCurrentUser.getFullName() + " updated " + task.getLabel());

        GetTaskResponse taskResponse = taskMapper.toGetResponse(task);

        return taskResponse;
    }

    @Override
    public List<GetTaskResponse> getTasksByProjectId(UUID projectId) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

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

            List<Issue> issues = issueRepository.findAllByTaskId(task.getId());
            List<GetIssueResponse> issueResponses = issues.stream().map(issue -> {
                GetIssueResponse issueResponse = new GetIssueResponse();
                issueResponse.setId(issue.getId());
                issueResponse.setLabel(issue.getLabel());
                issueResponse.setSummer(issue.getSummer());
                issueResponse.setDescription(issue.getDescription());
                issueResponse.setStartDate(issue.getStartDate());
                issueResponse.setDueDate(issue.getDueDate());
                issueResponse.setPriority(issue.getPriority().name());
                issueResponse.setStatus(issue.getStatus().name());
                issueResponse.setSeverity(issue.getSeverity().name());

                User issueAssignee = userRepository.findById(issue.getAssignee().getUser().getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
                GetUserResponse issueAssigneeResponse = userMapper.getUserResponse(issueAssignee);
                UserProfile issueAssigneeProfile = userProfileRepository.findByUserId(issueAssignee.getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));
                issueAssigneeResponse.setProfile(userProfileMapper.toProfile(issueAssigneeProfile));

                User issueReporter = userRepository.findById(issue.getReporter().getUser().getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
                GetUserResponse issueReporterResponse = userMapper.getUserResponse(issueReporter);
                UserProfile issueReporterProfile = userProfileRepository.findByUserId(issueReporter.getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));
                issueReporterResponse.setProfile(userProfileMapper.toProfile(issueReporterProfile));

                User issueCreatedBy = userRepository.findById(issue.getCreatedBy().getUser().getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
                GetUserResponse issueCreatedByResponse = userMapper.getUserResponse(issueCreatedBy);
                UserProfile issueCreatedByProfile = userProfileRepository.findByUserId(issueCreatedBy.getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));
                issueCreatedByResponse.setProfile(userProfileMapper.toProfile(issueCreatedByProfile));

                issueResponse.setAssignee(issueAssigneeResponse);
                issueResponse.setReporter(issueReporterResponse);
                issueResponse.setUser(issueCreatedByResponse);

                return issueResponse;
            }).collect(Collectors.toList());
            Drive driveService = null;
            try {
                driveService = googleDriveService.getDriveService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String fileId = googleDriveService.extractFileId(task.getAttachment());
            com.google.api.services.drive.model.File file = null;
            try {
                file = driveService.files().get(fileId).setFields("name, webViewLink, webContentLink").execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            GoogleDriveResponse googleDriveResponse = new GoogleDriveResponse();
            googleDriveResponse.setFileUrl(file.getWebViewLink());
            googleDriveResponse.setFileName(file.getName());
            googleDriveResponse.setDownloadUrl(file.getWebContentLink());

            List<GoogleDriveResponse> googleDriveResponses = new ArrayList<>();
            List<File> files = fileRepository.findByTaskId(task.getId());
            for (File fileTask : files) {
                String fileTaskId = googleDriveService.extractFileId(fileTask.getUrl());
                try {
                    com.google.api.services.drive.model.File driveFile = driveService.files()
                            .get(fileTaskId)
                            .setFields("name, webViewLink, webContentLink")
                            .execute();

                    GoogleDriveResponse fileResponse = new GoogleDriveResponse();
                    fileResponse.setFileName(driveFile.getName());
                    fileResponse.setFileUrl(driveFile.getWebViewLink());
                    fileResponse.setDownloadUrl(driveFile.getWebContentLink());

                    googleDriveResponses.add(fileResponse);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi lấy thông tin file từ Google Drive", e);
                }
            }

            GetTaskResponse taskResponse = taskMapper.toGetResponse(task);
            taskResponse.setUser(userResponse);
            taskResponse.setAttachment(googleDriveResponse);
            taskResponse.setAssignee(userAssigneeResponse);
            taskResponse.setReporter(userReporterResponse);
            taskResponse.setIssues(issueResponses);
            taskResponse.setFileResponse(googleDriveResponses);
            return taskResponse;
        }).collect(Collectors.toList());
        return responses;
    }

}
