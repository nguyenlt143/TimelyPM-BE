package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class GoogleAuthenticationController {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(GoogleAuthenticationController.class);
    private final IUserService userService;

//    @GetMapping("/api/user/google-auth/login")
//    public void login(HttpServletResponse response) throws IOException {
//        response.sendRedirect("/oauth2/authorization/google");
//    }

//    @GetMapping("/api/user/google-auth/success")
//    public ResponseEntity<Map<String, Object>> handleGoogleSuccess(@AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
//        logger.info("Request URL: {}", request.getRequestURL());
//        logger.info("SecurityContext authentication: {}", SecurityContextHolder.getContext().getAuthentication());
//        logger.info("OAuth2User principal: {}", principal != null ? principal.getAttributes() : "null");
//
//        Map<String, Object> response = new HashMap<>();
//        if (principal == null) {
//            logger.error("Failed to authenticate with Google - principal is null");
//            response.put("status", "error");
//            response.put("message", "Đăng nhập Google thất bại. Vui lòng thử lại.");
//            return ResponseEntity.status(401).body(response);
//        }
//
//        logger.info("Successfully authenticated with Google. User email: {}", Optional.ofNullable(principal.getAttribute("email")));
//        AuthenticateResponse authResponse = userService.handleGoogleLogin(principal);
//        response.put("status", "success");
//        response.put("data", authResponse);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/api/user/google-auth/signin-google")
//    public ResponseEntity<Map<String, Object>> handleGoogleCallback(@AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
//        logger.info("Request URL: {}", request.getRequestURL());
//        logger.info("Request params: {}", request.getParameterMap());
//        logger.info("SecurityContext authentication: {}", SecurityContextHolder.getContext().getAuthentication());
//        logger.info("OAuth2User principal: {}", principal != null ? principal.getAttributes() : "null");
//
//        Map<String, Object> response = new HashMap<>();
//        if (principal == null) {
//            logger.error("Failed to authenticate with Google - principal is null");
//            response.put("status", "error");
//            response.put("message", "Đăng nhập Google thất bại. Vui lòng thử lại hoặc kiểm tra quyền truy cập.");
//            return ResponseEntity.status(401).body(response);
//        }
//
//        logger.info("Successfully authenticated with Google. User email: {}", Optional.ofNullable(principal.getAttribute("email")));
//        AuthenticateResponse authResponse = userService.handleGoogleLogin(principal);
//        response.put("status", "success");
//        response.put("data", authResponse);
//        return ResponseEntity.ok(response);
//    }
}
