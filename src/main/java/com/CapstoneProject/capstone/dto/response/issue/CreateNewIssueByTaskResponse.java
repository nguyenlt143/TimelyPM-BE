package com.CapstoneProject.capstone.dto.response.issue;

import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNewIssueByTaskResponse {
    private UUID id;
    private String label;
    private String summer;
    private String description;
    private String attachment;
    private String attachmentName;
    private Date startDate;
    private Date dueDate;
    private String priority;
    private String severity;
    private GetUserResponse user;
    private GetUserResponse assignee;
    private GetUserResponse reporter;
}
