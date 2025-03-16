package com.CapstoneProject.capstone.service.impl.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpGenerate otpGenerator;
//    private final EmailService emailService;
//    private final EmailTemplateService emailTemplateService;

    public void sendOtp(String email) {
        // Tạo mã OTP
        Integer otp = otpGenerator.generateOTP(email);
        String subject = "Xác thực OTP";
        // Gửi OTP qua email
//        emailService.sendEmail(email, subject, emailTemplateService.buildOtpEmailTemplate(email, otp));
    }

    public void resendOtp(String email) {
        if (otpGenerator.isBlocked(email)) {
            throw new RuntimeException("Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau 5 phút.");
        }

        otpGenerator.clearOTPFromCache(email); // Xóa OTP cũ trước khi tạo OTP mới
        sendOtp(email); // Gửi OTP mới
    }

    public Boolean validateOTP(String key, Integer otpNumber) {
        if (otpGenerator.isBlocked(key)) {
//            throw new OTPRetryExceptional("Bạn đã nhập sai OTP quá nhiều lần. Vui lòng thử lại sau " + 5 + " phút.");
        }

        Integer cacheOTP = otpGenerator.getOTPByKey(key);
        if (cacheOTP != null && cacheOTP.equals(otpNumber)) {
            otpGenerator.clearOTPFromCache(key);
            return true;
        }

        otpGenerator.increaseFailedAttempts(key); // Tăng số lần nhập sai
        return false;
    }
}
