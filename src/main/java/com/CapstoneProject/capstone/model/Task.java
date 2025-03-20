package com.CapstoneProject.capstone.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Task extends BaseEntity{
    @OneToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;
}
