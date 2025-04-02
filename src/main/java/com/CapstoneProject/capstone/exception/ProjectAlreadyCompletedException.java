package com.CapstoneProject.capstone.exception;

public class ProjectAlreadyCompletedException extends RuntimeException {
    public ProjectAlreadyCompletedException(String message) {
        super(message);
    }
}
