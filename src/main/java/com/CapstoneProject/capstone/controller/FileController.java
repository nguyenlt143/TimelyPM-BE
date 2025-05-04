package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadFileProjectResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInIssueResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInTaskResponse;
import com.CapstoneProject.capstone.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.FILE.FILE)
public class FileController {
    private final IFileService fileService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = UrlConstant.FILE.ADD_FILE_IN_TASK, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UploadNewFileInTaskResponse>> uploadNewFileInTask(@PathVariable UUID id,
                                                                                         @RequestParam UUID projectId,
                                                                                         @RequestParam MultipartFile file) throws IOException {
        UploadNewFileInTaskResponse response = fileService.uploadNewFileInTask(id, projectId, file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Upload file thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = UrlConstant.FILE.ADD_FILE_IN_ISSUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UploadNewFileInIssueResponse>> uploadNewFileInIssue(@PathVariable UUID id,
                                                                                           @RequestParam UUID projectId,
                                                                                           @RequestParam MultipartFile file) throws IOException {
        UploadNewFileInIssueResponse response = fileService.uploadNewFileInIssue(id, projectId,file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Upload file thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = UrlConstant.FILE.ADD_FILE_TO_PROJECT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UploadFileProjectResponse>> uploadNewFileInIssue(@PathVariable UUID id,
                                                                                        @RequestParam MultipartFile file) throws IOException {
        UploadFileProjectResponse response = fileService.uploadFileProject(id,file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Upload file thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping(UrlConstant.FILE.DELETE_FILE)
    public ResponseEntity<BaseResponse<Boolean>> deleteFile(@PathVariable UUID id) throws IOException {
        boolean response = fileService.deleteFile(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Delete file thành công", response));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(value = UrlConstant.FILE.GET_ALL_FILES_IN_PROJECT)
    public ResponseEntity<BaseResponse<List<GoogleDriveResponse>>> getAllFileInProject(@PathVariable UUID id) throws IOException {
        List<GoogleDriveResponse> response = fileService.getAllFilesInProject(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Get all file thành công", response));
    }
}
