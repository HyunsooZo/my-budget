package com.mybudget.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("NaverSms 메세지 api 응답")
public class SmsApiResponseDto {
    String requestId;
    LocalDateTime requestTime;
    String statusCode;
    String statusName;
}
