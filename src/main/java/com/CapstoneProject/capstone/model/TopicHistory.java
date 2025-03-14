package com.CapstoneProject.capstone.model;

import com.CapstoneProject.capstone.enums.EntityTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TopicHistory extends BaseEntity{
    private EntityTypeEnum entityType;
    private int changeBy;
    private LocalDateTime changeDate;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonIgnore
    private Topic topic;
}
