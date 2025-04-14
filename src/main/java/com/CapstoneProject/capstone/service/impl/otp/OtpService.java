package com.CapstoneProject.capstone.service.impl.otp;

import com.CapstoneProject.capstone.exception.OTPRetryException;
import com.CapstoneProject.capstone.exception.ResendOTPException;
import com.CapstoneProject.capstone.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpGenerate otpGenerator;
    private final IEmailService emailService;

    public void sendOtp(String email) {
        Integer otp = otpGenerator.generateOTP(email);
        emailService.sendVerificationEmail(email, otp);
    }

    public void resendOtp(String email) {
        if (otpGenerator.isBlocked(email)) {
            throw new ResendOTPException("Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau 5 phút.");
        }

        otpGenerator.clearOTPFromCache(email);
        sendOtp(email);
    }

    public Boolean validateOTP(String key, Integer otpNumber) {
        if (otpGenerator.isBlocked(key)) {
            throw new OTPRetryException("Bạn đã nhập sai OTP quá nhiều lần. Vui lòng thử lại sau " + 5 + " phút.");
        }

        Integer cacheOTP = otpGenerator.getOTPByKey(key);
        if (cacheOTP != null && cacheOTP.equals(otpNumber)) {
            otpGenerator.clearOTPFromCache(key);
            return true;
        }

        otpGenerator.increaseFailedAttempts(key);
        return false;
    }

}
