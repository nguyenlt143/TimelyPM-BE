package com.CapstoneProject.capstone.model;


import com.CapstoneProject.capstone.enums.PriorityEnum;
import com.CapstoneProject.capstone.enums.QuestionStatusEnum;
import com.CapstoneProject.capstone.enums.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Question extends BaseEntity{
    private String label;
    private String summer;
    private String description;
    private String attachment;
    private Date startDate;
    private Date dueDate;
    @Enumerated(EnumType.STRING)
    private PriorityEnum priority;
    @Enumerated(EnumType.STRING)
    private QuestionStatusEnum status;
    @ManyToOne
    @JoinColumn(name = "assignee_to")
    @JsonIgnore
    private ProjectMember assignee;
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private ProjectMember createdBy;
    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonIgnore
    private Topic topic;
    @OneToMany(mappedBy = "question")
    private List<File> files;
    @OneToMany(mappedBy = "question")
    private List<Comment> comments;
}
