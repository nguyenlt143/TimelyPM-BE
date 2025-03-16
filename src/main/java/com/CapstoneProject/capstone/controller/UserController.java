package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;
import com.CapstoneProject.capstone.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse data = userService.registerUser(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Đăng ký thành công", data));
    }

    @PostMapping("/auth")
    public ResponseEntity<BaseResponse<AuthenticateResponse>> authenticate(@RequestBody @Valid AuthenticateRequest request) {
        AuthenticateResponse data = userService.authenticateUser(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Đăng nhập thành công", data));
    }
}
