package com.CapstoneProject.capstone.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class News extends BaseEntity{
    private String title;
    @Column(name = "content",columnDefinition = "TEXT")
    private String content;
}
