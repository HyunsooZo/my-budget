package com.mybudget.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("NaverSms 메세지 api 요청")
public class SmsApiRequestDto {
    String type;
    String contentType;
    String countryCode;
    String from;
    String content;
    List<SmsComponentDto> messages;

}
