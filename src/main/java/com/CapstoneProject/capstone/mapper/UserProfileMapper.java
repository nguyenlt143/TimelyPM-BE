package com.CapstoneProject.capstone.mapper;

import com.CapstoneProject.capstone.dto.response.profile.GetProfileResponse;
<<<<<<< HEAD
=======
import com.CapstoneProject.capstone.model.UserProfile;
>>>>>>> a17adb759a5f60a26e573478b71627fc5b7fb7d8
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
<<<<<<< HEAD
import org.springframework.context.annotation.Profile;
=======
>>>>>>> a17adb759a5f60a26e573478b71627fc5b7fb7d8
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileMapper {
    ModelMapper modelMapper;
<<<<<<< HEAD
    public GetProfileResponse toProfile(Profile profile) {
        return modelMapper.map(profile, GetProfileResponse.class);
=======
    public GetProfileResponse toProfile(UserProfile profile) {
        return modelMapper.map(profile, GetProfileResponse.class);

>>>>>>> a17adb759a5f60a26e573478b71627fc5b7fb7d8
    }
}
