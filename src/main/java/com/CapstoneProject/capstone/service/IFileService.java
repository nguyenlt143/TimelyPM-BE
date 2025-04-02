package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.response.file.UploadNewFileToTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface IFileService {
    UploadNewFileToTaskResponse uploadNewFileToTask(UUID taskId, MultipartFile file) throws IOException;
}
