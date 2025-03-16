package com.CapstoneProject.capstone.dto.request.user;

import com.CapstoneProject.capstone.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterUserInfoRequest {
    private String avatarUrl;
    private String gender;
    private String phone;
    private String fullName;
}
