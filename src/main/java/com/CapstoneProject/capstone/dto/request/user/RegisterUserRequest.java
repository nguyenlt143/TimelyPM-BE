package com.CapstoneProject.capstone.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterUserRequest {
    @NotNull(message = "Tên đăng nhập không được để trống")
    private String username;
    private String password;
    @NotNull(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
}
