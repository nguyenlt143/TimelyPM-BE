package com.CapstoneProject.capstone.dto.response.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GoogleDriveResponse {
    private String fileName;
    private String fileUrl;
    private String downloadUrl;
}
