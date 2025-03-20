package com.CapstoneProject.capstone.dto.response.task;

import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNewTaskResponse {
    private String label;
    private String summer;
    private String description;
    private String attachment;
    private Date startDate;
    private Date dueDate;
    private String priority;
    private String status;
    private GetUserResponse user;
}
