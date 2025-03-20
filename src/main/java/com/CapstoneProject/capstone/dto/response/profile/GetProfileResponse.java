package com.CapstoneProject.capstone.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetProfileResponse {
    private String avatarUrl;
    private String gender;
    private String phone;
    private String fullName;
}
