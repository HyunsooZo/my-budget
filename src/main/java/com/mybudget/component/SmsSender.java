package com.mybudget.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybudget.dto.SmsApiRequestDto;
import com.mybudget.dto.SmsApiResponseDto;
import com.mybudget.dto.SmsComponentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmsSender {

    @Value("${naver.sms.ServiceId}")
    private String serviceId;
    @Value("${naver.sms.accessKey}")
    private String accessKey;
    @Value("${naver.sms.secretKey}")
    private String secretKey;
    @Value("${naver.sms.senderPhone}")
    private String senderPhone;
    @Value("${naver.sms.headerTime}")
    private String headerTime;
    @Value("${naver.sms.headerKey}")
    private String headerKey;
    @Value("${naver.sms.headerSign}")
    private String headerSign;

    private final String SMS_URL = "https://sens.apigw.ntruss.com/sms/v2/services/";


    /**
     * SMS를 발송하는 메서드
     *
     * @param smsComponentDto SMS 발송에 필요한 정보를 담은 SmsComponentDto
     * @throws Exception SMS 발송 중 발생할 수 있는 예외
     */
    @Async
    public void sendSms(SmsComponentDto smsComponentDto) throws Exception {

        Long time = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(headerTime, time.toString());
        headers.set(headerKey, accessKey);
        headers.set(headerSign, makeSignature(time));

        List<SmsComponentDto> messages = new ArrayList<>();
        messages.add(smsComponentDto);

        SmsApiRequestDto request = SmsApiRequestDto.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(senderPhone)
                .content(smsComponentDto.getContent())
                .messages(messages)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        SmsApiResponseDto result =
                restTemplate.postForObject(
                        new URI(SMS_URL + serviceId + "/messages"),
                        httpBody,
                        SmsApiResponseDto.class);

        log.info(String.valueOf(result));
    }

    /**
     * 네이버에서 제공하는 자바 시그니처 생성 메서드
     *
     * @param time 현재 시간을 나타내는 Long 값
     * @return 생성된 시그니처 문자열
     * @throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
     */
    public String makeSignature(Long time) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + this.serviceId + "/messages";
        String timestamp = time.toString();
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey =
                new SecretKeySpec(
                        secretKey.getBytes("UTF-8"),
                        "HmacSHA256"
                );

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));

        return Base64.encodeBase64String(rawHmac);
    }
}
