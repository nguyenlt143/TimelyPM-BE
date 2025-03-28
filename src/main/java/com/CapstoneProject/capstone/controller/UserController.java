package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;
import com.CapstoneProject.capstone.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UrlConstant.USER.USER)
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    @PostMapping(UrlConstant.USER.REGISTER)
    public ResponseEntity<BaseResponse<RegisterResponse>> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse data = userService.registerUser(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Đăng ký thành công", data));
    }

    @PostMapping(UrlConstant.USER.LOGIN)
    public ResponseEntity<BaseResponse<AuthenticateResponse>> authenticate(@RequestBody @Valid AuthenticateRequest request) {
        AuthenticateResponse data = userService.authenticateUser(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Đăng nhập thành công", data));
    }

    @GetMapping(UrlConstant.USER.GET)
    public ResponseEntity<BaseResponse<GetUserResponse>> getUser() {
        GetUserResponse data = userService.getUser();
        return ResponseEntity.ok(new BaseResponse<>("200", "Hồ sơ người dùng", data));
    }
}
