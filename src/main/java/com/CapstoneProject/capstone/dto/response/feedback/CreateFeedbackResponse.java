package com.CapstoneProject.capstone.dto.response.feedback;

import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateFeedbackResponse {
    private UUID id;
    private String feedback;
    private GetUserResponse user;
}
