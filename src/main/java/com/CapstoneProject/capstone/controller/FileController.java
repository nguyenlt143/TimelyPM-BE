package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInIssueResponse;
import com.CapstoneProject.capstone.dto.response.file.UploadNewFileInTaskResponse;
import com.CapstoneProject.capstone.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlConstant.FILE.FILE)
public class FileController {
    private final IFileService fileService;

    @PostMapping(value = UrlConstant.FILE.Add_FILE_IN_TASK, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UploadNewFileInTaskResponse>> uploadNewFileInTask(@PathVariable UUID id, @RequestParam MultipartFile file) throws IOException {
        UploadNewFileInTaskResponse response = fileService.uploadNewFileInTask(id, file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Upload file thành công", response));
    }

    @PostMapping(value = UrlConstant.FILE.Add_FILE_IN_ISSUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UploadNewFileInIssueResponse>> uploadNewFileInIssue(@PathVariable UUID id, @RequestParam MultipartFile file) throws IOException {
        UploadNewFileInIssueResponse response = fileService.uploadNewFileInIssue(id, file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Upload file thành công", response));
    }
}
