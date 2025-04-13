package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.auth.ChangePasswordRequest;
import com.CapstoneProject.capstone.dto.request.auth.UpdateProfileRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;
import com.CapstoneProject.capstone.model.User;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IUserService {
    RegisterResponse registerUser(RegisterRequest request);

    AuthenticateResponse authenticateUser(AuthenticateRequest request);

    GetUserResponse getUser();

    GetUserResponse getUser(UUID id);

    Boolean deleteUser(UUID id);

    List<GetUserResponse> getUsers();

    Boolean changePassword(ChangePasswordRequest request);

    Boolean uploadAvatar(MultipartFile file) throws IOException, InterruptedException;

    GetUserResponse updateProfile(UpdateProfileRequest request);

    AuthenticateResponse handleGoogleLogin(OAuth2User principal);
    AuthenticateResponse loginGoogle(String accessToken) throws FirebaseAuthException;
}

