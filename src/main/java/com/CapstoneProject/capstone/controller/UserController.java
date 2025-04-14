package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.auth.ChangePasswordRequest;
import com.CapstoneProject.capstone.dto.request.auth.UpdateProfileRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.GetUserResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;
import com.CapstoneProject.capstone.service.IUserService;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    @PostMapping(UrlConstant.USER.CHANGE_PASSWORD)
    public ResponseEntity<BaseResponse<Boolean>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        boolean response = userService.changePassword(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Change password successful", response));
    }

    @PostMapping(UrlConstant.USER.UPLOAD_AVATAR)
    public ResponseEntity<BaseResponse<Boolean>> uploadAvatar(@RequestPart MultipartFile file) throws IOException, InterruptedException {
        boolean response = userService.uploadAvatar(file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Upload avatar successful", response));
    }

    @PostMapping(UrlConstant.USER.UPDATE_PROFILE)
    public ResponseEntity<BaseResponse<GetUserResponse>> updateProfile(@RequestBody UpdateProfileRequest request) {
        GetUserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Update profile successful", response));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(UrlConstant.USER.GET_ALL)
    public ResponseEntity<BaseResponse<List<GetUserResponse>>> getAllUser() {
        List<GetUserResponse> data = userService.getUsers();
        return ResponseEntity.ok(new BaseResponse<>("200", "Danh sách người dùng", data));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(UrlConstant.USER.GET_BY_ID)
    public ResponseEntity<BaseResponse<GetUserResponse>> getUserById(@PathVariable UUID id) {
        GetUserResponse data = userService.getUser(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Hồ sơ người dùng", data));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(UrlConstant.USER.DELETE)
    public ResponseEntity<BaseResponse<Boolean>> getUser(@PathVariable UUID id) {
        boolean data = userService.deleteUser(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa người dùng thành công", data));
    }

    @PostMapping(UrlConstant.USER.LOGIN_GOOGLE)
    public ResponseEntity<BaseResponse<AuthenticateResponse>> loginGoogle(@RequestParam String accessToken) throws FirebaseAuthException {
        AuthenticateResponse data = userService.loginGoogle(accessToken);
        return ResponseEntity.ok(new BaseResponse<>("200", "Login google successful", data));
    }

    @PostMapping(UrlConstant.USER.VERIFY_EMAIL)
    public ResponseEntity<BaseResponse<Boolean>> verifyAccount(@RequestParam String email, @RequestParam Integer otp) {
        boolean data = userService.verifyAccount(email, otp);
        return ResponseEntity.ok(new BaseResponse<>("200", "Verify account successful", data));
    }

    @PostMapping(UrlConstant.USER.RESEND_EMAIL)
    public ResponseEntity<BaseResponse<Boolean>> resendAccount(@RequestParam String email) {
        boolean data = userService.resendOtp(email);
        return ResponseEntity.ok(new BaseResponse<>("200", "Resend otp account successful", data));
    }
}
