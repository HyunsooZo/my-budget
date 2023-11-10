package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOtpGenerationRequestDto {

    @Pattern(regexp = "\\d+", message = "휴대폰 번호는 숫자로만 입력해주세요.")
    @NotBlank(message = "휴대폰 번호를 입력해주세요.")
    private String phoneNumber;
}
