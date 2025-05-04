package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.question.CreateNewQuestionRequest;
import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.dto.response.question.CreateNewQuestionResponse;
import com.CapstoneProject.capstone.dto.response.question.GetQuestionResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.enums.*;
import com.CapstoneProject.capstone.exception.*;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.mapper.UserProfileMapper;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.INotificationService;
import com.CapstoneProject.capstone.service.IProjectActivityLogService;
import com.CapstoneProject.capstone.service.IQuestionService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TopicRepository topicRepository;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final IProjectActivityLogService projectActivityLogService;
    private final INotificationService notificationService;
    private final RoleRepository roleRepository;

    @Override
    public CreateNewQuestionResponse createNewQuestion(UUID projectId, UUID topicId, CreateNewQuestionRequest request) {
        String priorityStr = request.getPriority();
        PriorityEnum priority;
        try {
            priority = PriorityEnum.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Priority không hợp lệ! Chỉ chấp nhận LOW, MEDIUM, HIGH.");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        if(project.getStatus().equals(StatusEnum.PENDING.name()) || project.getStatus().equals(StatusEnum.DONE.name())){
            throw new ProjectAlreadyCompletedException("Không thể thêm question mới vì dự án chưa bắt đầu hoặc đã kết thúc.");
        }

        UUID userId = AuthenUtil.getCurrentUserId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));


        ProjectMember creator = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Optional<String> maxQuestionLabelOpt = questionRepository.findMaxQuestionLabelByTopicId(topicId);
        int newQuestionNumber = 1;
        if (maxQuestionLabelOpt.isPresent()) {
            String maxTaskLabel = maxQuestionLabelOpt.get();

            String[] parts = maxTaskLabel.split("-");
            String lastPart = parts[parts.length - 1];
            try {
                newQuestionNumber = Integer.parseInt(lastPart);
                newQuestionNumber++;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi khi chuyển đổi số: " + e.getMessage());
            }
        }
        String questionLabel = String.format("%s-%s-Question-%03d", project.getName(), topic.getLabels(), newQuestionNumber);



        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberId(projectId, request.getAssigneeTo(), "APPROVED").orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên này trong dự án"));
        Question question = new Question();
        question.setLabel(questionLabel);
        question.setSummer(request.getSummer());
        question.setDescription(request.getDescription());
        question.setAttachment(request.getAttachment());
        question.setAssignee(projectMember);
        question.setCreatedBy(creator);
        question.setStartDate(request.getStartDate());
        question.setDueDate(request.getDueDate());
        question.setStatus(QuestionStatusEnum.NEW);
        question.setPriority(priority);
        question.setActive(true);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setTopic(topic);
        questionRepository.save(question);

        User assigneeUser = userRepository.findById(projectMember.getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng assignee"));

        Notification assigneeNotification = new Notification();
        assigneeNotification.setMessage(String.format("Bạn được giao question mới '%s' bởi %s trong dự án %s",
                questionLabel, profileCurrentUser.getFullName(), project.getName()));
        assigneeNotification.setRead(false);
        assigneeNotification.setUser(assigneeUser);
        assigneeNotification.setProject(project);
        assigneeNotification.setActive(true);
        assigneeNotification.setCreatedAt(LocalDateTime.now());
        assigneeNotification.setUpdatedAt(LocalDateTime.now());
        notificationService.createNotification(assigneeNotification);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.CREATE_QUESTION, profileCurrentUser.getFullName() + " created " + question.getLabel());

        GetUserResponse userResponse = userMapper.getUserResponse(assigneeUser);
        UserProfile userProfile = profileRepository.findByUserId(assigneeUser.getId()).get();
        GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
        userResponse.setProfile(profileResponse);
        CreateNewQuestionResponse response = new CreateNewQuestionResponse();
        response.setId(question.getId());
        response.setLabel(question.getLabel());
        response.setSummer(question.getSummer());
        response.setDescription(question.getDescription());
        response.setAttachment(question.getAttachment());
        response.setStartDate(question.getStartDate());
        response.setDueDate(question.getDueDate());
        response.setPriority(question.getPriority().toString());
        response.setStatus(question.getStatus().toString());
        response.setUser(userResponse);
        return response;
    }

    @Override
    public List<GetQuestionResponse> getQuestions(UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Role role = roleRepository.findByName(RoleEnum.PM);

        List<Question> questions;
        if (projectMember.getRole() == role) {
            questions = questionRepository.findByTopicId(topicId);
        } else {
            questions = questionRepository.findByTopicIdAndUserId(topicId, userId);
        }

        List<GetQuestionResponse> responses = new ArrayList<>();
        for(Question question : questions){
            GetQuestionResponse response = new GetQuestionResponse();
            response.setId(question.getId());
            response.setLabel(question.getLabel());
            response.setSummer(question.getSummer());
            response.setDescription(question.getDescription());
            response.setAttachment(question.getAttachment());
            response.setStartDate(question.getStartDate());
            response.setDueDate(question.getDueDate());
            response.setPriority(question.getPriority().toString());
            response.setStatus(question.getStatus().toString());
            User user = userRepository.findById(question.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

            GetUserResponse userResponse = userMapper.getUserResponse(user);

            UserProfile userProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse profileResponse = userProfileMapper.toProfile(userProfile);
            userResponse.setProfile(profileResponse);

            User createBy = userRepository.findById(question.getCreatedBy().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

            GetUserResponse createByResponse = userMapper.getUserResponse(createBy);

            UserProfile createByProfile = profileRepository.findByUserId(createBy.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng này"));

            GetProfileResponse createByProfileResponse = userProfileMapper.toProfile(createByProfile);
            createByResponse.setProfile(createByProfileResponse);
            response.setAssignee(userResponse);
            response.setCreateBy(createByResponse);
            responses.add(response);
        }
        return responses;
    }

    @Override
    public GetQuestionResponse getQuestion(UUID id, UUID projectId, UUID topicId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        UUID userId = AuthenUtil.getCurrentUserId();

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(()-> new NotFoundException("Bạn không phải thành viên của project này"));

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));
        if(!topic.getProject().getId().equals(project.getId())){
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Question question = questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy câu hỏi này"));

        boolean isPM = (question.getCreatedBy() != null && question.getCreatedBy().getUser() != null)
                && question.getCreatedBy().getUser().getId().equals(userId);

        boolean isAssignee = (question.getAssignee() != null && question.getAssignee().getUser() != null)
                && question.getAssignee().getUser().getId().equals(userId);

        boolean isCreateBy = (question.getCreatedBy() != null && question.getCreatedBy().getUser() != null)
                && question.getCreatedBy().getUser().getId().equals(userId);

        if (!isPM && !isAssignee && !isCreateBy) {
            throw new ForbiddenException("Bạn không có quyền xem câu hỏi này");
        }

        User createBy = userRepository.findById(question.getCreatedBy().getUser().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
        GetUserResponse creatorResponse = userMapper.getUserResponse(createBy);
        UserProfile creatorProfile = profileRepository.findByUserId(createBy.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người tạo câu hỏi"));
        creatorResponse.setProfile(userProfileMapper.toProfile(creatorProfile));


        GetUserResponse assigneeResponse = null;
        if (question.getAssignee() != null && question.getAssignee().getUser() != null) {
            User assignee = userRepository.findById(question.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao câu hỏi"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao câu hỏi"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }
        GetQuestionResponse questionResponse = new GetQuestionResponse();
        questionResponse.setId(id);
        questionResponse.setLabel(question.getLabel());
        questionResponse.setSummer(question.getSummer());
        questionResponse.setDescription(question.getDescription());
        questionResponse.setAttachment(question.getAttachment());
        questionResponse.setStartDate(question.getStartDate());
        questionResponse.setDueDate(question.getDueDate());
        questionResponse.setPriority(question.getPriority().toString());
        questionResponse.setStatus(question.getStatus().toString());
        questionResponse.setAssignee(assigneeResponse);
        questionResponse.setCreateBy(creatorResponse);
        return questionResponse;
    }

    @Override
    public GetQuestionResponse updateQuestion(UUID id, UUID projectId, UUID topicId, String status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy project"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topic"));

        if (!topic.getProject().getId().equals(project.getId())) {
            throw new InvalidProjectException("Topic không thuộc project đã chỉ định");
        }

        Question question = questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy question"));
        UUID userId = AuthenUtil.getCurrentUserId();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserProfile profileCurrentUser = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng"));

        boolean isCreateBy = question.getCreatedBy() != null && question.getCreatedBy().getUser().getId().equals(userId);

        if(!isCreateBy){
            throw new ForbiddenException("Bạn không có quyền update question, chỉ có người tạo mới được update");
        }
        QuestionStatusEnum questionStatusEnum;
        try {
            questionStatusEnum = QuestionStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Trạng thái không hợp lệ! Các trạng thái hợp lệ: " +
                    Arrays.toString(QuestionStatusEnum.values()));
        }

        if (question.getStatus() != QuestionStatusEnum.NEW) {
            throw new InvalidEnumException("Chỉ có thể cập nhật question từ trạng thái NEW, question này đã DONE");
        }

        if (!questionStatusEnum.equals(QuestionStatusEnum.DONE)) {
            throw new InvalidEnumException("Chỉ có thể cập nhật trạng thái thành DONE");
        }

        question.setStatus(QuestionStatusEnum.DONE);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);

        projectActivityLogService.logActivity(project, currentUser, ActivityTypeEnum.UPDATE_QUESTION,
                profileCurrentUser.getFullName() + " đã cập nhật trạng thái " + question.getLabel() + " thành DONE");

        GetUserResponse creatorResponse = userMapper.getUserResponse(currentUser);
        creatorResponse.setProfile(userProfileMapper.toProfile(profileCurrentUser));


        GetUserResponse assigneeResponse = null;
        if (question.getAssignee() != null && question.getAssignee().getUser() != null) {
            User assignee = userRepository.findById(question.getAssignee().getUser().getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người được giao câu hỏi"));

            assigneeResponse = userMapper.getUserResponse(assignee);
            UserProfile assigneeProfile = profileRepository.findByUserId(assignee.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người được giao câu hỏi"));

            assigneeResponse.setProfile(userProfileMapper.toProfile(assigneeProfile));
        }
        GetQuestionResponse questionResponse = new GetQuestionResponse();
        questionResponse.setId(id);
        questionResponse.setLabel(question.getLabel());
        questionResponse.setSummer(question.getSummer());
        questionResponse.setDescription(question.getDescription());
        questionResponse.setAttachment(question.getAttachment());
        questionResponse.setStartDate(question.getStartDate());
        questionResponse.setDueDate(question.getDueDate());
        questionResponse.setPriority(question.getPriority().toString());
        questionResponse.setStatus(question.getStatus().toString());
        questionResponse.setAssignee(assigneeResponse);
        questionResponse.setCreateBy(creatorResponse);
        return questionResponse;
    }
}
