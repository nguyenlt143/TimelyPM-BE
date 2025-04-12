package com.CapstoneProject.capstone.dto.response.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetCommentResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private UUID questionId;
    private String content;
    private String createdAt;
}
