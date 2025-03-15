package com.CapstoneProject.capstone.exception;

public class UserExisted extends RuntimeException{
    public UserExisted(String message) {
        super(message);
    }
}
