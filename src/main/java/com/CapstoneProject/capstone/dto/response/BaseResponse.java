package com.CapstoneProject.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BaseResponse<T> {
    private String code;
    private String message;
    private T data;
}
