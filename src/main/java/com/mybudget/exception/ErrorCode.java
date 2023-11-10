package com.mybudget.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    //undefined
    UNDEFINED_EXCEPTION(BAD_REQUEST, "알 수 없는 오류입니다."),
    //user
    USER_INFO_NOT_FOUND(NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
    EXISTING_USER(BAD_REQUEST, "이미 가입한 사용자 입니다."),
    INVALID_OTP(BAD_REQUEST, "유효하지 않은 인증번호 입니다."),
    INVALID_PASSWORD(BAD_REQUEST,"비밀번호가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
