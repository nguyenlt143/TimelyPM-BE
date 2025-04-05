package com.CapstoneProject.capstone.dto.request.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateTaskRequest {
    private String summer;
    private String description;
    private Date startDate;
    private Date dueDate;
    private String priority;
}
