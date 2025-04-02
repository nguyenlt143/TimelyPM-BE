package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileToTaskResponse;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.File;
import com.CapstoneProject.capstone.model.Task;
import com.CapstoneProject.capstone.repository.FileRepository;
import com.CapstoneProject.capstone.repository.TaskRepository;
import com.CapstoneProject.capstone.service.IFileService;
import com.CapstoneProject.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements IFileService {
    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final GoogleDriveService googleDriveService;
    @Override
    public UploadNewFileToTaskResponse uploadNewFileToTask(UUID taskId, MultipartFile file) throws IOException {
        UUID userId = AuthenUtil.getCurrentUserId();

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));

        GoogleDriveResponse url = googleDriveService.uploadFileToDrive(file);

        File newFile = new File();
        newFile.setName(url.getFileName());
        newFile.setUrl(url.getFileUrl());
        newFile.setTask(task);
        newFile.setActive(true);
        newFile.setCreatedAt(LocalDateTime.now());
        newFile.setUpdatedAt(LocalDateTime.now());
        fileRepository.save(newFile);

        UploadNewFileToTaskResponse response = new UploadNewFileToTaskResponse();
        response.setTaskId(newFile.getTask().getId());
        response.setName(newFile.getName());
        response.setUrl(url.getFileUrl());
        return response;
    }
}
