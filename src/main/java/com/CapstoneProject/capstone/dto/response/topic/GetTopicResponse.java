package com.CapstoneProject.capstone.dto.response.topic;

import com.CapstoneProject.capstone.model.Topic;
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
public class GetTopicResponse {
    private UUID id;
    private UUID projectId;
    private String type;
    private String description;
    private String labels;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    public GetTopicResponse(Topic topic) {
        this.id = topic.getId();
        this.type = topic.getType();
        this.description = topic.getDescription();
        this.labels = topic.getLabels();
        this.startDate = topic.getStartDate();
        this.dueDate = topic.getDueDate();
        this.projectId = (topic.getProject() != null) ? topic.getProject().getId() : null;
    }
}
