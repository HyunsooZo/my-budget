package com.mybudget.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    //undefined
    UNDEFINED_EXCEPTION(BAD_REQUEST, "알 수 없는 오류입니다.");

    private final HttpStatus status;
    private final String message;
}
