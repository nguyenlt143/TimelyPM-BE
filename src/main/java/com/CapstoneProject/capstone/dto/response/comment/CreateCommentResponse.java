package com.CapstoneProject.capstone.dto.response.comment;

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
public class CreateCommentResponse {
    private UUID id;
    private UUID userId;
    private UUID questionId;
    private String content;
    private LocalDateTime createdAt;
}
