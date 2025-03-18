package com.CapstoneProject.capstone.mapper;

import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
import com.CapstoneProject.capstone.model.UserProfile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileMapper {
    ModelMapper modelMapper;
    public GetProfileResponse toProfile(UserProfile profile) {
        return modelMapper.map(profile, GetProfileResponse.class);

    }
}
