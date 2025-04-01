package com.CapstoneProject.capstone.dto.response.task;

import com.CapstoneProject.capstone.dto.response.file.GoogleDriveResponse;
import com.CapstoneProject.capstone.dto.response.issue.GetIssueResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetTaskResponse {
    private UUID id;
    private String label;
    private String summer;
    private String description;
    private GoogleDriveResponse attachment;
    private Date startDate;
    private Date dueDate;
    private String priority;
    private String status;
    private GetUserResponse user;
    private GetUserResponse assignee;
    private GetUserResponse reporter;
    private List<GetIssueResponse> issues;
}
