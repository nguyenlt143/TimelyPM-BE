package com.CapstoneProject.capstone.dto.response.projectMember;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetMemberPendingRespone {
    private UUID id;
    private String avatarUrl;
    private String fullName;
}
