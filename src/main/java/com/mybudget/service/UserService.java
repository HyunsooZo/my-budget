package com.mybudget.service;

import com.mybudget.component.SmsSender;
import com.mybudget.domain.User;
import com.mybudget.dto.SmsComponentDto;
import com.mybudget.dto.UserOtpGenerationRequestDto;
import com.mybudget.dto.UserSignUpRequestDto;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mybudget.enums.SmsTexts.OTP_TEXT;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final SmsSender smsSender;
    private final StringRedisTemplate redisTemplate;
    private static final String OTP_KEY = "OTP: ";

    public void sendOtp(UserOtpGenerationRequestDto userOtpGenerationRequestDto) {

        String phoneNumber = userOtpGenerationRequestDto.getPhoneNumber();
        String otp = generateOtp();

        SmsComponentDto smsComponentDto = SmsComponentDto.builder()
                .to(phoneNumber)
                .content(String.format(OTP_TEXT, otp))
                .build();

        try {
            smsSender.sendSms(smsComponentDto);
        } catch (Exception e) {
            log.error("SMS 발송 실패 {}", e.getMessage());
        }

        redisTemplate.opsForValue().set(
                OTP_KEY + phoneNumber, otp, 3, TimeUnit.MINUTES
        );

    }

    private String generateOtp() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6);
    }

}
