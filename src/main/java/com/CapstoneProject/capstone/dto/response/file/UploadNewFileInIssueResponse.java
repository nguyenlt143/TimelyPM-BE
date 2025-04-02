package com.CapstoneProject.capstone.dto.response.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadNewFileInIssueResponse {
    private UUID issueId;
    private String name;
    private String url;
}
