package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.issue.CreateNewIssueRequest;
import com.CapstoneProject.capstone.dto.request.issue.UpdateIssueRequest;
import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.issue.CreateNewIssueResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.enums.*;
import com.CapstoneProject.capstone.exception.*;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.IIssueService;
import com.CapstoneProject.capstone.service.INotificationService;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import com.google.api.services.drive.Drive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final TaskRepository taskRepository;
    private final UserProfileRepository userProfileRepository;
    private final GoogleDriveService googleDriveService;
    private final FileRepository fileRepository;
    private final IProjectActivityLogService projectActivityLogService;
    private final INotificationService notificationService;

    @Override
    public CreateNewIssueResponse createNewIssue(UUID projectId, UUID topicId, CreateNewIssueRequest request, MultipartFile file) throws IOException {
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
            throw new InvalidEnumException("Severity không hợp lệ! Chỉ chấp nhận MINOR, MAJOR, CRITICAL");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        if(project.getStatus().equals(StatusEnum.PENDING.name()) || project.getStatus().equals(StatusEnum.DONE.name())){
            throw new ProjectAlreadyCompletedException("Không thể thêm issue mới vì dự án chưa bắt đầu hoặc đã kết thúc.");
        }

        UUID userId = AuthenUtil.getCurrentUserId();
//        User currentUser = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));


        List<User> pmUsers = userRepository.findUsersWithRolePMOrQAByProjectId(projectId);
        User currentPM = pmUsers.stream()
                .filter(pm -> pm.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("Bạn không có quyền"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentPM.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);
        Optional<String> maxIssueLabelOpt = issueRepository.findMaxIssueLabelByTopicId(topicId);
        int newIssueNumber = 1;
        if (maxIssueLabelOpt.isPresent()) {
            String maxTaskLabel = maxIssueLabelOpt.get();

            String[] parts = maxTaskLabel.split("-");
            String lastPart = parts[parts.length - 1];
            try {
                newIssueNumber = Integer.parseInt(lastPart);
                newIssueNumber++;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi khi chuyển đổi số: " + e.getMessage());
            }
        }
        String issueLabel = String.format("%s-%s-Issue-%03d", project.getName(), topic.getLabels(), newIssueNumber);

        ProjectMember assignee = projectMemberRepository.findByProjectIdAndMemberId(projectId, request.getAssigneeTo(), "APPROVED").orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
        ProjectMember reporter = projectMemberRepository.findByProjectIdAndMemberId(projectId, request.getReporter(), "APPROVED").orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
        ProjectMember pmMember = projectMemberRepository.findByProjectIdAndUserId(projectId, currentPM.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên"));

        Issue issue = new Issue();
        issue.setLabel(issueLabel);
        issue.setSummer(request.getSummer());
        issue.setDescription(request.getDescription());
        issue.setAttachment(url.getFileUrl());
        issue.setAssignee(assignee);
        issue.setReporter(reporter);
        issue.setCreatedBy(pmMember);
        issue.setStartDate(request.getStartDate());
        issue.setDueDate(request.getDueDate());
        issue.setStatus(IssueStatusEnum.OPEN);
        issue.setPriority(priority);
        issue.setSeverity(severity);
        issue.setActive(true);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());
        issue.setTopic(topic);
        issueRepository.save(issue);

//        UserProfile pmProfile = profileRepository.findByUserId(pmMember.getUser().getId())
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));
        User assigneeUser = userRepository.findById(assignee.getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng assignee"));
        User reporterUser = userRepository.findById(reporter.getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng reporter"));

        Notification assigneeNotification = new Notification();
        assigneeNotification.setMessage(String.format("Bạn được giao issue mới '%s' bởi %s trong dự án %s",
                issueLabel, profileCurrentUser.getFullName(), project.getName()));
        assigneeNotification.setRead(false);
        assigneeNotification.setUser(assigneeUser);
        assigneeNotification.setProject(project);
        assigneeNotification.setActive(true);
        assigneeNotification.setCreatedAt(LocalDateTime.now());
        assigneeNotification.setUpdatedAt(LocalDateTime.now());
        notificationService.createNotification(assigneeNotification);

        Notification reporterNotification = new Notification();
        reporterNotification.setMessage(String.format("Issue mới '%s' được tạo bởi %s trong dự án %s, bạn là reporter",
                issueLabel, profileCurrentUser.getFullName(), project.getName()));
        reporterNotification.setRead(false);
        reporterNotification.setUser(reporterUser);
        reporterNotification.setProject(project);
        reporterNotification.setActive(true);
        reporterNotification.setCreatedAt(LocalDateTime.now());
        reporterNotification.setUpdatedAt(LocalDateTime.now());
        notificationService.createNotification(reporterNotification);

//        User user = userRepository.findById(assignee.getUser().getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
        UserProfile userProfile = profileRepository.findByUserId(assigneeUser.getId()).get();

        projectActivityLogService.logActivity(project, currentPM, ActivityTypeEnum.CREATE_ISSUE, profileCurrentUser.getFullName() + " created new issue successfully");

        GetUserResponse userResponse = userMapper.getUserResponse(assigneeUser);
        GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
        userResponse.setProfile(profileResponse);
        CreateNewIssueResponse response = new CreateNewIssueResponse();
        response.setId(issue.getId());
        response.setLabel(issue.getLabel());
        response.setSummer(issue.getSummer());
        response.setDescription(issue.getDescription());
        response.setAttachment(url.getFileUrl());
        response.setAttachmentName(url.getFileName());
        response.setStartDate(issue.getStartDate());
        response.setDueDate(issue.getDueDate());
        response.setPriority(issue.getPriority().toString());
        response.setSeverity(issue.getSeverity().toString());
        response.setStatus(issue.getStatus().toString());
        response.setUser(userResponse);
        return response;
    }

    @Override
    public List<GetIssueResponse> getIssues(UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        List<Issue> issues = issueRepository.findByTopicId(topicId);
        List<GetIssueResponse> responses = issues.stream().map(issue -> {
            User user = userRepository.findById(issue.getCreatedBy().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userResponse = userMapper.getUserResponse(user);
            UserProfile userProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            User assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userAssigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile userProfileAssignee = userProfileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            User reporter = userRepository.findById(issue.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userReporterResponse = userMapper.getUserResponse(reporter);
            UserProfile userProfileReporter = userProfileRepository.findByUserId(reporter.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
            GetProfileResponse userProfileAssigneeResponse = userProfileMapper.toProfile(userProfileAssignee);
            GetProfileResponse userProfileReporterResponse = userProfileMapper.toProfile(userProfileReporter);

            userAssigneeResponse.setProfile(userProfileAssigneeResponse);
            userResponse.setProfile(profileResponse);
            userReporterResponse.setProfile(userProfileReporterResponse);

            Drive driveService = null;
            try {
                driveService = googleDriveService.getDriveService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String fileId = googleDriveService.extractFileId(issue.getAttachment());
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
            List<File> files = fileRepository.findByTaskId(issue.getId());
            for (File fileIssue : files) {
                String fileIssueId = googleDriveService.extractFileId(fileIssue.getUrl());
                try {
                    com.google.api.services.drive.model.File driveFile = driveService.files()
                            .get(fileIssueId)
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

            GetIssueResponse issueResponse = new GetIssueResponse();
            issueResponse.setId(issue.getId());
            issueResponse.setLabel(issue.getLabel());
            issueResponse.setSummer(issue.getSummer());
            issueResponse.setDescription(issue.getDescription());
            issueResponse.setAttachment(googleDriveResponse);
            issueResponse.setStartDate(issue.getStartDate());
            issueResponse.setDueDate(issue.getDueDate());
            issueResponse.setPriority(issue.getPriority().toString());
            issueResponse.setSeverity(issue.getSeverity().toString());
            issueResponse.setStatus(issue.getStatus().toString());
            issueResponse.setUser(userResponse);
            issueResponse.setAssignee(userAssigneeResponse);
            issueResponse.setReporter(userReporterResponse);
            issueResponse.setFileResponses(googleDriveResponses);
            return issueResponse;
        }).collect(Collectors.toList());
        return responses;
    }

    @Override
    public GetIssueResponse getIssue(UUID id, UUID projectId, UUID topicId) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy issue"));

        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        boolean isPM = (issue.getCreatedBy() != null && issue.getCreatedBy().getUser() != null)
                && issue.getCreatedBy().getUser().getId().equals(userId);

        boolean isAssignee = (issue.getAssignee() != null && issue.getAssignee().getUser() != null)
                && issue.getAssignee().getUser().getId().equals(userId);

        boolean isReporter = (issue.getReporter() != null && issue.getReporter().getUser() != null)
                && issue.getReporter().getUser().getId().equals(userId);

        if (!isPM && !isAssignee && !isReporter) {
            throw new ForbiddenException("Bạn không có quyền xem issue này");
        }

        User creator = userRepository.findById(issue.getCreatedBy().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người tạo issue"));
        GetUserResponse creatorResponse = userMapper.getUserResponse(creator);

        UserProfile creatorProfile = profileRepository.findByUserId(creator.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người tạo issue"));
        creatorResponse.setProfile(userProfileMapper.toProfile(creatorProfile));

        GetUserResponse assigneeResponse = null;
        if (issue.getAssignee() != null && issue.getAssignee().getUser() != null) {
            User assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao issue"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao issue"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }

        GetUserResponse reporterResponse = null;
        if (issue.getReporter() != null && issue.getReporter().getUser() != null) {
            User reporter = userRepository.findById(issue.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người báo cáo issue"));

            reporterResponse = userMapper.getUserResponse(reporter);
            UserProfile reporterProfile = profileRepository.findByUserId(reporter.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người báo cáo issue"));

            reporterResponse.setProfile(userProfileMapper.toProfile(reporterProfile));
        }

        Drive driveService = googleDriveService.getDriveService();
        String fileId = googleDriveService.extractFileId(issue.getAttachment());
        com.google.api.services.drive.model.File file = driveService.files().get(fileId).setFields("name, webViewLink, webContentLink").execute();
        GoogleDriveResponse googleDriveResponse = new GoogleDriveResponse();
        googleDriveResponse.setFileUrl(file.getWebViewLink());
        googleDriveResponse.setFileName(file.getName());
        googleDriveResponse.setDownloadUrl(file.getWebContentLink());

        List<GoogleDriveResponse> googleDriveResponses = new ArrayList<>();
        List<File> files = fileRepository.findByTaskId(issue.getId());
        for (File fileIssue : files) {
            String fileIssueId = googleDriveService.extractFileId(fileIssue.getUrl());
            try {
                com.google.api.services.drive.model.File driveFile = driveService.files()
                        .get(fileIssueId)
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

        GetIssueResponse issueResponse = new GetIssueResponse();
        issueResponse.setId(issue.getId());
        issueResponse.setLabel(issue.getLabel());
        issueResponse.setDescription(issue.getDescription());
        issueResponse.setSummer(issue.getSummer());
        issueResponse.setAttachment(googleDriveResponse);
        issueResponse.setStartDate(issue.getStartDate());
        issueResponse.setDueDate(issue.getDueDate());
        issueResponse.setPriority(issue.getPriority().name());
        issueResponse.setStatus(issue.getStatus().name());
        issueResponse.setSeverity(issue.getSeverity().name());
        issueResponse.setUser(creatorResponse);
        issueResponse.setAssignee(assigneeResponse);
        issueResponse.setReporter(reporterResponse);
        issueResponse.setFileResponses(googleDriveResponses);

        return issueResponse;
    }


    @Override
    public GetIssueResponse getIssueByTask(UUID id, UUID projectId, UUID topicId, UUID taskId) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy task"));

        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        boolean isPM = userRepository.findUserWithRolePMByProjectId(projectId)
                .map(pmUser -> pmUser.getId().equals(userId))
                .orElse(false);

        Issue issue = issueRepository.findByIdAndTaskId(id, taskId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy issue với id này trong task"));

        boolean isAssignee = (issue.getAssignee() != null && issue.getAssignee().getUser() != null)
                && issue.getAssignee().getUser().getId().equals(userId);

        boolean isReporter = (issue.getReporter() != null && issue.getReporter().getUser() != null)
                && issue.getReporter().getUser().getId().equals(userId);

        if (!isPM && !isAssignee && !isReporter) {
            throw new ForbiddenException("Bạn không có quyền xem issue này");
        }

        User creator = userRepository.findById(issue.getCreatedBy().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người tạo issue"));
        GetUserResponse creatorResponse = userMapper.getUserResponse(creator);

        UserProfile creatorProfile = profileRepository.findByUserId(creator.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người tạo issue"));
        creatorResponse.setProfile(userProfileMapper.toProfile(creatorProfile));

        GetUserResponse assigneeResponse = null;
        if (issue.getAssignee() != null && issue.getAssignee().getUser() != null) {
            User assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao issue"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao issue"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }

        GetUserResponse reporterResponse = null;
        if (issue.getReporter() != null && issue.getReporter().getUser() != null) {
            User reporter = userRepository.findById(issue.getReporter().getUser().getId())
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

        GetIssueResponse response = new GetIssueResponse();
        response.setId(issue.getId());
        response.setLabel(issue.getLabel());
        response.setSummer(issue.getSummer());
        response.setDescription(issue.getDescription());
        response.setAttachment(googleDriveResponse);
        response.setStartDate(issue.getStartDate());
        response.setDueDate(issue.getDueDate());
        response.setPriority(issue.getPriority().toString());
        response.setStatus(issue.getStatus().toString());
        response.setSeverity(issue.getSeverity().name());
        response.setUser(creatorResponse);
        response.setAssignee(assigneeResponse);
        response.setReporter(reporterResponse);
        return response;
    }

    @Override
    public GetIssueResponse updateIssue(UUID id, UUID projectId, UUID topicId, String status) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy issue"));

        UUID userId = AuthenUtil.getCurrentUserId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        UserProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ"));

        boolean isAssignee = issue.getAssignee() != null && issue.getAssignee().getUser().getId().equals(userId);
        boolean isReporter = issue.getReporter().getUser().getId().equals(userId);

        if (!isAssignee && !isReporter) {
            throw new ForbiddenException("Bạn không có quyền cập nhật issue này");
        }

        IssueStatusEnum newStatus;
        try {
            newStatus = IssueStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Trạng thái không hợp lệ! Các trạng thái hợp lệ: " +
                    Arrays.toString(IssueStatusEnum.values()));
        }

        IssueStatusEnum currentStatus = issue.getStatus();
        boolean isValidTransition = false;
        List<IssueStatusEnum> assigneeStatuses = Arrays.asList(IssueStatusEnum.NOT_BUG, IssueStatusEnum.FIXED, IssueStatusEnum.PENDING_RETEST);

        if (assigneeStatuses.contains(newStatus) && !isAssignee) {
            throw new ForbiddenException("Chỉ assignee mới có thể chuyển trạng thái sang " + newStatus);
        }
        if (!assigneeStatuses.contains(newStatus) && !isReporter) {
            throw new ForbiddenException("Chỉ reporter mới có thể chuyển trạng thái sang " + newStatus);
        }

        switch (currentStatus) {
            case OPEN:
            case RE_OPENED:
                isValidTransition = newStatus == IssueStatusEnum.NOT_BUG || newStatus == IssueStatusEnum.FIXED;
                break;
            case FIXED:
                isValidTransition = newStatus == IssueStatusEnum.PENDING_RETEST;
                break;
            case PENDING_RETEST:
                isValidTransition = newStatus == IssueStatusEnum.RETEST;
                break;
            case RETEST:
                isValidTransition = newStatus == IssueStatusEnum.RE_OPENED || newStatus == IssueStatusEnum.VERIFIED;
                break;
            case VERIFIED:
                isValidTransition = newStatus == IssueStatusEnum.CLOSED;
                break;
            case NOT_BUG:
            case CLOSED:
                isValidTransition = false;
                break;
        }

        if (!isValidTransition) {
            throw new InvalidEnumException("Chuyển đổi trạng thái không hợp lệ! Từ " + currentStatus +
                    ", chỉ có thể chuyển sang: " + getAllowedTransitions(currentStatus));
        }

        User creator = userRepository.findById(issue.getCreatedBy().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người tạo issue"));
        GetUserResponse creatorResponse = userMapper.getUserResponse(creator);

        UserProfile creatorProfile = profileRepository.findByUserId(creator.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người tạo issue"));
        creatorResponse.setProfile(userProfileMapper.toProfile(creatorProfile));

        GetUserResponse assigneeResponse = null;
        UserProfile assigneeProfile = null;
        User assignee = null;
        if (issue.getAssignee() != null && issue.getAssignee().getUser() != null) {
            assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao issue"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao issue"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }

        issue.setStatus(newStatus);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.UPDATE_ISSUE,
                profile.getFullName() + " updated status " + issue.getLabel() + " successfully");

        Drive driveService = googleDriveService.getDriveService();
        String fileId = googleDriveService.extractFileId(issue.getAttachment());
        com.google.api.services.drive.model.File file = driveService.files().get(fileId)
                .setFields("name, webViewLink, webContentLink").execute();
        GoogleDriveResponse googleDriveResponse = new GoogleDriveResponse();
        googleDriveResponse.setFileUrl(file.getWebViewLink());
        googleDriveResponse.setFileName(file.getName());
        googleDriveResponse.setDownloadUrl(file.getWebContentLink());

        GetIssueResponse issueResponse = new GetIssueResponse();
        issueResponse.setId(issue.getId());
        issueResponse.setLabel(issue.getLabel());
        issueResponse.setSummer(issue.getSummer());
        issueResponse.setDescription(issue.getDescription());
        issueResponse.setAttachment(googleDriveResponse);
        issueResponse.setStartDate(issue.getStartDate());
        issueResponse.setDueDate(issue.getDueDate());
        issueResponse.setPriority(issue.getPriority().toString());
        issueResponse.setStatus(issue.getStatus().toString());
        issueResponse.setSeverity(issue.getSeverity().toString());
        issueResponse.setUser(creatorResponse);
        issueResponse.setAssignee(assigneeResponse);
        return issueResponse;
    }

    private String getAllowedTransitions(IssueStatusEnum currentStatus) {
        switch (currentStatus) {
            case OPEN:
            case RE_OPENED:
                return "[NOT_BUG, FIXED]";
            case FIXED:
                return "[PENDING_RETEST]";
            case PENDING_RETEST:
                return "[RETEST]";
            case RETEST:
                return "[RE_OPENED, VERIFIED]";
            case VERIFIED:
                return "[CLOSED]";
            default:
                return "[]";
        }
    }

    @Override
    public Boolean deleteIssue(UUID id, UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy issue"));

        UUID userId = AuthenUtil.getCurrentUserId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        boolean isPM = userRepository.findUserWithRolePMByProjectId(projectId)
                .map(pmUser -> pmUser.getId().equals(userId))
                .orElse(false);

        boolean isAssignee = (issue.getAssignee() != null && issue.getAssignee().getUser() != null)
                && issue.getAssignee().getUser().getId().equals(userId);

        boolean isReporter = (issue.getReporter() != null && issue.getReporter().getUser() != null)
                && issue.getReporter().getUser().getId().equals(userId);

        if (!isPM && !isAssignee && !isReporter) {
            throw new ForbiddenException("Bạn không có quyền xóa issue này");
        }

        issue.setActive(false);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.DELETE_ISSUE, profile.getFullName() + " deleted " + issue.getLabel() + " successfully");

        return true;
    }

    @Override
    public GetIssueResponse UpdateIssue(UUID id, UUID projectId, UUID topicId, UpdateIssueRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        UserProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ"));

        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));
        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Không tìm thấy Project Manager"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Issue issue = issueRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy issue trong module này"));

        PriorityEnum priority = null;
        if (request.getPriority() != null){
            String prioritySt = request.getPriority();
            try{
                priority = PriorityEnum.valueOf(prioritySt.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }

        SeverityEnum severity = null;
        if (request.getSeverity() != null){
            String severitySt = request.getSeverity();
            try{
                severity = SeverityEnum.valueOf(severitySt.toUpperCase());
            }catch (IllegalArgumentException | NullPointerException e){
                throw new InvalidEnumException("Trạng thái không hợp lệ");
            }
        }

        issue.setSummer(request.getSummer() == null ? issue.getSummer() : request.getSummer());
        issue.setDescription(request.getDescription() == null ? issue.getDescription() : request.getDescription());
        issue.setStartDate(request.getStartDate() == null ? issue.getStartDate() : request.getStartDate());
        issue.setDueDate(request.getDueDate() == null ? issue.getDueDate() : request.getDueDate());
        issue.setPriority(request.getPriority() == null ? issue.getPriority() : priority);
        issue.setSeverity(request.getSeverity() == null ? issue.getSeverity() : severity);
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.UPDATE_ISSUE, profile.getFullName() + " updated issue " + issue.getLabel() + " successfully");

        GetIssueResponse issueResponse = new GetIssueResponse();
        issueResponse.setId(issue.getId());
        issueResponse.setSummer(issue.getSummer());
        issueResponse.setDescription(issue.getDescription());
        issueResponse.setStartDate(issue.getStartDate());
        issueResponse.setDueDate(issue.getDueDate());
        issueResponse.setPriority(issue.getPriority().toString());
        issueResponse.setSeverity(issue.getSeverity().toString());
        issueResponse.setStatus(issue.getStatus().toString());

        return issueResponse;
    }

    @Override
    public List<GetIssueResponse> getIssuesByProjectId(UUID projectId) {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy dự án này"));

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Bạn không phải thành viên của project này"));

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        List<GetIssueResponse> responses = issues.stream().map(issue -> {
            User user = userRepository.findById(issue.getCreatedBy().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userResponse = userMapper.getUserResponse(user);
            UserProfile userProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            User assignee = userRepository.findById(issue.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userAssigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile userProfileAssignee = userProfileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            User reporter = userRepository.findById(issue.getReporter().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
            GetUserResponse userReporterResponse = userMapper.getUserResponse(reporter);
            UserProfile userProfileReporter = userProfileRepository.findByUserId(reporter.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
            GetProfileResponse userProfileAssigneeResponse = userProfileMapper.toProfile(userProfileAssignee);
            GetProfileResponse userProfileReporterResponse = userProfileMapper.toProfile(userProfileReporter);

            userAssigneeResponse.setProfile(userProfileAssigneeResponse);
            userResponse.setProfile(profileResponse);
            userReporterResponse.setProfile(userProfileReporterResponse);

            Drive driveService = null;
            try {
                driveService = googleDriveService.getDriveService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String fileId = googleDriveService.extractFileId(issue.getAttachment());
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
            List<File> files = fileRepository.findByTaskId(issue.getId());
            for (File fileIssue : files) {
                String fileIssueId = googleDriveService.extractFileId(fileIssue.getUrl());
                try {
                    com.google.api.services.drive.model.File driveFile = driveService.files()
                            .get(fileIssueId)
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

            GetIssueResponse issueResponse = new GetIssueResponse();
            issueResponse.setId(issue.getId());
            issueResponse.setLabel(issue.getLabel());
            issueResponse.setSummer(issue.getSummer());
            issueResponse.setDescription(issue.getDescription());
            issueResponse.setAttachment(googleDriveResponse);
            issueResponse.setStartDate(issue.getStartDate());
            issueResponse.setDueDate(issue.getDueDate());
            issueResponse.setPriority(issue.getPriority().toString());
            issueResponse.setSeverity(issue.getSeverity().toString());
            issueResponse.setStatus(issue.getStatus().toString());
            issueResponse.setUser(userResponse);
            issueResponse.setAssignee(userAssigneeResponse);
            issueResponse.setReporter(userReporterResponse);
            issueResponse.setFileResponses(googleDriveResponses);
            return issueResponse;
        }).collect(Collectors.toList());
        return responses;

    }
}
