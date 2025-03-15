package com.CapstoneProject.capstone.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthenticateResponse {
    private UUID id;
    private String username;
    private String token;
    private List<String> role;
}
