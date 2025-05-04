package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadFileProjectResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInIssueResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInTaskResponse;
import com.CapstoneProject.capstone.exception.ForbiddenException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.*;
import com.CapstoneProject.capstone.repository.*;
import com.CapstoneProject.capstone.service.IFileService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import com.google.api.services.drive.Drive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements IFileService {
    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final GoogleDriveService googleDriveService;
    private final UserRepository userRepository;
    private final GoogleDriveService driveService;
    @Override
    public UploadNewFileInTaskResponse uploadNewFileInTask(UUID taskId, UUID projectId, MultipartFile file) throws IOException {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Project Member Not Found"));

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));

        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);

        File newFile = new File();
        newFile.setName(url.getFileName());
        newFile.setUrl(url.getFileUrl());
        newFile.setProjectMember(projectMember);
        newFile.setTask(task);
        newFile.setActive(true);
        newFile.setCreatedAt(LocalDateTime.now());
        newFile.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(newFile);

        UploadNewFileInTaskResponse response = new UploadNewFileInTaskResponse();
        response.setTaskId(newFile.getTask().getId());
        response.setName(newFile.getName());
        response.setUrl(url.getFileUrl());
        return response;
    }

    @Override
    public UploadNewFileInIssueResponse uploadNewFileInIssue(UUID issueId, UUID projectId, MultipartFile file) throws IOException {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));

        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElseThrow(() -> new NotFoundException("Project Member Not Found"));

        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new NotFoundException("Issue not found"));

        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);

        File newFile = new File();
        newFile.setName(url.getFileName());
        newFile.setUrl(url.getFileUrl());
        newFile.setProjectMember(projectMember);
        newFile.setIssue(issue);
        newFile.setActive(true);
        newFile.setCreatedAt(LocalDateTime.now());
        newFile.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(newFile);

        UploadNewFileInIssueResponse response = new UploadNewFileInIssueResponse();
        response.setIssueId(newFile.getIssue().getId());
        response.setName(newFile.getName());
        response.setUrl(url.getFileUrl());
        return response;
    }

    @Override
    public UploadFileProjectResponse uploadFileProject(UUID projectId, MultipartFile file) throws IOException {
        UUID userId = AuthenUtil.getCurrentUserId();
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));
        User pmUser = userRepository.findUserWithRolePMByProjectId(projectId).orElseThrow(()-> new NotFoundException("Không tìm thấy Project Manager"));
        if(!pmUser.getId().equals(userId)){
            throw new ForbiddenException("Bạn không có quyền");
        }

        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);

        File newFile = new File();
        newFile.setName(url.getFileName());
        newFile.setUrl(url.getFileUrl());
        newFile.setProject(project);
        newFile.setActive(true);
        newFile.setCreatedAt(LocalDateTime.now());
        newFile.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(newFile);

        UploadFileProjectResponse response = new UploadFileProjectResponse();
        response.setId(newFile.getId());
        response.setProjectId(project.getId());
        response.setName(newFile.getName());
        response.setUrl(url.getFileUrl());
        return response;
    }

    @Override
    public List<GoogleDriveResponse> getAllFilesInProject(UUID projectId) throws IOException {
        UUID userId = AuthenUtil.getCurrentUserId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Project Member not found"));

        List<File> files = fileRepository.findByProjectId(projectId);

        List<GoogleDriveResponse> responses = new ArrayList<>();
        Drive driveService = googleDriveService.getDriveService();
        for (File file : files) {
            try {
                String fileId = googleDriveService.extractFileId(file.getUrl());
                com.google.api.services.drive.model.File driveFile = driveService.files()
                        .get(fileId)
                        .setFields("name, webViewLink, webContentLink")
                        .execute();

                GoogleDriveResponse response = new GoogleDriveResponse();
                response.setId(file.getId());
                response.setFileName(driveFile.getName());
                response.setFileUrl(driveFile.getWebViewLink());
                response.setDownloadUrl(driveFile.getWebContentLink());

                responses.add(response);
            } catch (IOException e) {
                throw new IOException("Error retrieving file information from Google Drive", e);
            }
        }

        return responses;
    }

    @Override
    public Boolean deleteFile(UUID id) throws IOException {
        File file = fileRepository.findById(id).orElseThrow(() -> new NotFoundException("File not found"));
        fileRepository.delete(file);
        return true;
    }
}
