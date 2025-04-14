package com.CapstoneProject.capstone.service;

public interface IEmailService {
    public void sendVerificationEmail(String email, Integer otp);
}
