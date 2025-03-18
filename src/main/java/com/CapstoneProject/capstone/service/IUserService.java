package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;

public interface IUserService {
    RegisterResponse registerUser(RegisterRequest request);
    AuthenticateResponse authenticateUser(AuthenticateRequest request);
    GetUserResponse getUser();
}
