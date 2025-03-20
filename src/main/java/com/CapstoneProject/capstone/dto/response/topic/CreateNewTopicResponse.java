package com.CapstoneProject.capstone.dto.response.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNewTopicResponse {
    private UUID id;
    private UUID projectId;
    private String type;
    private String description;
    private String labels;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
}
