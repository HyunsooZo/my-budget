package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOtpVerificationRequestDto {
    @Pattern(regexp = "\\d+", message = "휴대폰 번호는 숫자로만 입력해주세요.")
    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    private String phoneNumber;

    @Size(min = 6, max = 6, message = "OTP는 6자리로 입력해주세요.")
    @NotBlank
    private String otp;
}
