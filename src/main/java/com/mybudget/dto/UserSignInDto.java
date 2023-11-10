package com.mybudget.dto;

import com.mybudget.config.UserRole;
import com.mybudget.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInDto {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String phoneNumber;

    public static UserSignInDto from(User user, String accessToken, String refreshToken) {
        return UserSignInDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
