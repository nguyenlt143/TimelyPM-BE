package com.CapstoneProject.capstone.dto.response.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String fullName;
    private UUID questionId;
    private String avatarUrl;
    private String content;
    private String createdAt;
}
