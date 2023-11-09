package com.mybudget.dto;

import com.mybudget.config.UserRole;
import com.mybudget.domain.User;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel("토큰 발급 Dto")
public class TokenIssuanceDto {
    private Long id;
    private String email;
    private UserRole userRole;

    public static TokenIssuanceDto from(User user) {
        return TokenIssuanceDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .build();
    }
}
