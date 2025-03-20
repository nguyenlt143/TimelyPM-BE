package com.CapstoneProject.capstone.model;

import com.CapstoneProject.capstone.enums.TopicTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Topic extends BaseEntity{
    private String type;
    private String description;
    private String labels;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    @OneToOne
    @JoinColumn(name = "priority_id")
    private Priority priority;
    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;
    @OneToMany(mappedBy = "topic")
    private List<TopicComment> topicComments;
    @OneToMany(mappedBy = "topic")
    private List<TopicHistory> topicHistories;
    @OneToMany(mappedBy = "topic")
    private List<File> files;
    @OneToMany(mappedBy = "topic")
    private List<Task> tasks;
    @OneToMany(mappedBy = "topic")
    private List<Issue> issues;
    @OneToMany(mappedBy = "topic")
    private List<Question> questions;
}
