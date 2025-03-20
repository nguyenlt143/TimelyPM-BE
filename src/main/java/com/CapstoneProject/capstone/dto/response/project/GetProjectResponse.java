package com.CapstoneProject.capstone.dto.response.project;

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
public class GetProjectResponse {
    private UUID id;
    private String name;
    private String image;
    private String status;
    private GetProfileResponse profile;
}
