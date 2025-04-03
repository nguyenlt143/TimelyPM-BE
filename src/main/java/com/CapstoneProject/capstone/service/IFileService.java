package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInIssueResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface IFileService {
    UploadNewFileInTaskResponse uploadNewFileInTask(UUID taskId, UUID projectId, MultipartFile file) throws IOException;
    UploadNewFileInIssueResponse uploadNewFileInIssue(UUID issueId, UUID projectId, MultipartFile file) throws IOException;

}
