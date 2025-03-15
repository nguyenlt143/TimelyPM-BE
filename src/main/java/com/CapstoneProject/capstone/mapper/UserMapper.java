package com.CapstoneProject.capstone.mapper;

import com.CapstoneProject.capstone.dto.request.user.RegisterUserInfoRequest;
import com.CapstoneProject.capstone.dto.request.user.RegisterUserRequest;
import com.CapstoneProject.capstone.dto.response.user.RegisterUserInfoResponse;
import com.CapstoneProject.capstone.dto.response.user.RegisterUserResponse;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.model.UserProfile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMapper {
    ModelMapper modelMapper;

    public User toModel(RegisterUserRequest request){
        return modelMapper.map(request, User.class);
    }

    public UserProfile toProfile(RegisterUserInfoRequest request){
        return modelMapper.map(request, UserProfile.class);
    }

    public RegisterUserResponse toResponse(User user){
        return modelMapper.map(user, RegisterUserResponse.class);
    }

    public RegisterUserInfoResponse toResponse(UserProfile userProfile){
        return modelMapper.map(userProfile, RegisterUserInfoResponse.class);
    }
}
