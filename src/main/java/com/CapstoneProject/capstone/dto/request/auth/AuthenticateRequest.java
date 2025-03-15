package com.CapstoneProject.capstone.dto.request.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthenticateRequest {
    @NotNull(message = "Tên đăng nhập không được để trống")
    private String username;
    @NotNull(message = "Mật khẩu không được để trống")
    private String password;
}
