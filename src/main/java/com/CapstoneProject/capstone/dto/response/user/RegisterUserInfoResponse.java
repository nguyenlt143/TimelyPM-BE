package com.CapstoneProject.capstone.dto.response.user;

import com.CapstoneProject.capstone.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterUserInfoResponse {
    private String avatarUrl;
    private String gender;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
}
