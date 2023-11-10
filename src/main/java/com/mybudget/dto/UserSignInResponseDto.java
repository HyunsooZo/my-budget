package com.mybudget.dto;

import com.mybudget.config.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSignInResponseDto {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String phoneNumber;

    public static UserSignInResponseDto from(UserSignInDto userSignInDto) {
        return UserSignInResponseDto.builder()
                .accessToken(userSignInDto.getAccessToken())
                .refreshToken(userSignInDto.getRefreshToken())
                .userId(userSignInDto.getUserId())
                .email(userSignInDto.getEmail())
                .phoneNumber(userSignInDto.getPhoneNumber())
                .build();
    }
}
