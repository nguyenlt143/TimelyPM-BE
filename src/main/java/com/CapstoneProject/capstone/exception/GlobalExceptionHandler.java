package com.CapstoneProject.capstone.exception;

import com.CapstoneProject.capstone.dto.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //Validation not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleNotFoundException(NotFoundException ex) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setCode("404");
        response.setMessage(ex.getMessage());
        response.setData(null);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    //Bắt lỗi đăng nhập sai
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BaseResponse<>("401", "Tên đăng nhập hoặc mật khẩu không đúng", null));
    }

    //Validation(Null, Size, Email)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getDefaultMessage());
        }

        BaseResponse<List<String>> response = new BaseResponse<>();
        response.setCode("400");
        response.setMessage(errors.get(0));
        response.setData(null);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //Validation Gender
    @ExceptionHandler(InvalidEnumException.class)
    public ResponseEntity<BaseResponse<?>> handleInvalidEnumException(InvalidEnumException ex) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setCode("400");
        response.setMessage(ex.getMessage());
        response.setData(null);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserExisted.class)
    public ResponseEntity<BaseResponse<?>> handleUserExisted(UserExisted ex) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setCode("400");
        response.setMessage(ex.getMessage());
        response.setData(null);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<BaseResponse<?>> forbiddenException(ForbiddenException ex) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setCode("403");
        response.setMessage(ex.getMessage());
        response.setData(null);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>("500", "Lỗi máy chủ", null));
    }

}
