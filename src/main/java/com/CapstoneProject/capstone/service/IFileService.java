package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadFileProjectResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInIssueResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IFileService {
    UploadNewFileInTaskResponse uploadNewFileInTask(UUID taskId, UUID projectId, MultipartFile file) throws IOException;
    UploadNewFileInIssueResponse uploadNewFileInIssue(UUID issueId, UUID projectId, MultipartFile file) throws IOException;
    UploadFileProjectResponse uploadFileProject(UUID projectId, MultipartFile file) throws IOException;
    List<GoogleDriveResponse> getAllFilesInProject(UUID projectId) throws IOException;
    Boolean deleteFile(UUID id) throws IOException;
}
