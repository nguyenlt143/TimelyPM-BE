package com.CapstoneProject.capstone.dto.request.issue;

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
public class CreateNewIssueRequest {
    private UUID assigneeTo;
    private String label;
    private String summer;
    private String description;
    private String attachment;
    private Date startDate;
    private Date dueDate;
    private String priority;
    private String severity;
}
