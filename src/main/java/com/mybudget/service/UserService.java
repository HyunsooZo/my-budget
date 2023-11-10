package com.mybudget.service;

import com.mybudget.component.EmailSender;
import com.mybudget.component.SmsSender;
import com.mybudget.domain.User;
import com.mybudget.dto.SmsComponentDto;
import com.mybudget.dto.UserOtpGenerationRequestDto;
import com.mybudget.dto.UserSignUpRequestDto;
import com.mybudget.exception.CustomException;
import com.mybudget.exception.ErrorCode;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mybudget.enums.MailTexts.VERIFICATION_SUBJECT;
import static com.mybudget.enums.MailTexts.VERIFICATION_TEXT;
import static com.mybudget.enums.SmsTexts.OTP_TEXT;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final String OTP_KEY = "OTP: ";

    /**
     * 사용자에게 OTP를 생성하고 전송
     *
     * @param userOtpGenerationRequestDto 사용자 OTP 생성 요청 DTO
     */
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

        // OTP를 Redis에 저장하고 3분 동안 유지
        redisTemplate.opsForValue().set(
                OTP_KEY + phoneNumber, otp, 3, TimeUnit.MINUTES
        );

    }

    /**
     * 6자리의 랜덤한 OTP를 생성
     *
     * @return 생성된 OTP 문자열
     */
    private String generateOtp() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6);
    }

    /**
     * 사용자를 등록하고 이메일 인증 메일을 전송
     *
     * @param userSignUpRequestDto 사용자 회원가입 요청 DTO
     */
    @Transactional
    public void signUp(UserSignUpRequestDto userSignUpRequestDto) {

        // 이메일 중복 확인
        userRepository.findByEmail(userSignUpRequestDto.getEmail())
                .ifPresent(user -> {
                    throw new CustomException(ErrorCode.EXISTING_USER);
                });

        // 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(userSignUpRequestDto.getPassword());

        // 사용자 저장
        User user = userRepository.save(
                User.from(userSignUpRequestDto, encodedPassword)
        );

        // 이메일 인증 메일 전송
        emailSender.sendEmail(
                user.getEmail(),
                VERIFICATION_SUBJECT,
                String.format(VERIFICATION_TEXT, user.getId())
        );
    }
}