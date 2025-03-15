package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.auth.AuthenticateRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterRequest;
import com.CapstoneProject.capstone.dto.response.auth.AuthenticateResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterResponse;
import com.CapstoneProject.capstone.enums.GenderEnum;
import com.CapstoneProject.capstone.exception.InvalidEnumException;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.exception.UserExisted;
import com.CapstoneProject.capstone.mapper.UserMapper;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.model.UserProfile;
import com.CapstoneProject.capstone.model.UserRole;
import com.CapstoneProject.capstone.repository.UserProfileRepository;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        String genderStr = request.getUserInfo().getGender();

        GenderEnum gender;
        try {
            gender = GenderEnum.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEnumException("Giới tính không hợp lệ! Chỉ chấp nhận MALE hoặc FEMALE.");
        }

        if (userRepository.findByUsername(request.getUser().getUsername()).isPresent()) {
            throw new UserExisted("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.findByEmail(request.getUser().getEmail()).isPresent()) {
            throw new UserExisted("Email đã tồn tại!");
        }
        User user = userMapper.toModel(request.getUser());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        UserProfile userProfile = userMapper.toProfile(request.getUserInfo());
        userProfile.setUser(user);
        userProfile.setGender(request.getUserInfo().getGender());
        userProfileRepository.save(userProfile);
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setUser(userMapper.toResponse(user));
        registerResponse.setUserInfo(userMapper.toResponse(userProfile));

        return registerResponse;
    }

    @Override
    public AuthenticateResponse authenticateUser(AuthenticateRequest request) {
        Authentication authentication;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
//        List<String> roles = user.getUserRoles().stream()
//                .map(userRole -> userRole.getRole().getName())
//                .collect(Collectors.toList());
        var jwtToken = jwtService.generateToken(user);

        AuthenticateResponse response = new AuthenticateResponse();
        response.setToken(jwtToken);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
//        response.setRole(roles);
        return response;
    }
}
