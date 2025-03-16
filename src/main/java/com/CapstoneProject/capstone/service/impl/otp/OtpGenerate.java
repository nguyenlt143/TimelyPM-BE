package com.CapstoneProject.capstone.service.impl.otp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpGenerate {
    private static final Integer EXPIRE_MIN = 5; // OTP có hiệu lực trong 5 phút
    private static final Integer MAX_RETRIES = 5; // Tối đa 5 lần gửi OTP trong 10 phút
    private static final Integer RETRY_EXPIRE_MIN = 5; // Hết hạn giới hạn sau 10 phút
    private static final Integer MAX_WRONG_ATTEMPTS = 5; // Tối đa 5 lần nhập sai OTP

    private LoadingCache<String, Integer> otpCache;
    private LoadingCache<String, Integer> retryCache;
    private LoadingCache<String, Integer> failedAttemptsCache;

    public OtpGenerate() {
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MIN, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String s) {
                        return 0;
                    }
                });

        // Cache giới hạn số lần gửi OTP (5 lần trong 10 phút)
        retryCache = CacheBuilder.newBuilder()
                .expireAfterWrite(RETRY_EXPIRE_MIN, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String s) {
                        return 0;
                    }
                });

        // Cache giới hạn số lần nhập sai OTP (5 lần sai trong 10 phút)
        failedAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(RETRY_EXPIRE_MIN, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String s) {
                        return 0;
                    }
                });
    }

    public Integer generateOTP(String key) {
        int retries = retryCache.getIfPresent(key) == null ? 0 : retryCache.getIfPresent(key);
        if (retries >= MAX_RETRIES) {
            throw new RuntimeException("Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau " + RETRY_EXPIRE_MIN + " phút.");
        }

        SecureRandom secureRandom = new SecureRandom();
        int OTP = 100000 + secureRandom.nextInt(900000); // Sinh mã OTP ngẫu nhiên
        otpCache.put(key, OTP);

        retryCache.put(key, retries + 1);
        return OTP;
    }

    public Integer getOTPByKey(String key) {
        return otpCache.getIfPresent(key);
    }

    public void clearOTPFromCache(String key) {
        otpCache.invalidate(key);
        failedAttemptsCache.invalidate(key); // Xóa luôn số lần nhập sai nếu xác thực thành công
    }

    public void increaseFailedAttempts(String key) {
        int attempts = failedAttemptsCache.getIfPresent(key) == null ? 0 : failedAttemptsCache.getIfPresent(key);
        failedAttemptsCache.put(key, attempts + 1);
    }

    public boolean isBlocked(String key) {
        int attempts = failedAttemptsCache.getIfPresent(key) == null ? 0 : failedAttemptsCache.getIfPresent(key);
        return attempts >= MAX_WRONG_ATTEMPTS;
    }
}
