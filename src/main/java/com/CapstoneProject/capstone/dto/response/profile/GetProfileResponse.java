package com.CapstoneProject.capstone.dto.response.profile;

import com.CapstoneProject.capstone.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetProfileResponse {
    private String avatarUrl;
    private String gender;
    private String phone;
    private String fullName;
    public GetProfileResponse(UserProfile userProfile) {
        this.avatarUrl = userProfile.getAvatarUrl();
        this.gender = userProfile.getGender();
        this.phone = userProfile.getPhone();
        this.fullName = userProfile.getFullName();
    }
}
