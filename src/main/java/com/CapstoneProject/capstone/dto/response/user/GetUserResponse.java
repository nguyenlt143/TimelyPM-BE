package com.CapstoneProject.capstone.dto.response.user;

import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUserResponse {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private GetProfileResponse profile;
}
