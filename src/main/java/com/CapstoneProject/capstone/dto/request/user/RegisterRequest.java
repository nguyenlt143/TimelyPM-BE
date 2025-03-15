package com.CapstoneProject.capstone.dto.request.user;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    @Valid
    private RegisterUserRequest user;
    private RegisterUserInfoRequest userInfo;
}
