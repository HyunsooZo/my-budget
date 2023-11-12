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
    INVALID_PASSWORD(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    //budget
    BUDGET_AMOUNT_NOT_MATCH(BAD_REQUEST, "예산의 총액과 각 예산의 총액이 일치하지 않습니다."),
    BUDGET_HASNT_BEEN_SET(BAD_REQUEST, "예산이 설정 되지 않은 사용자 입니다."),
    BUDGET_NOT_FOUND(NOT_FOUND, "예산 정보를 찾을 수 없습니다."),
    NOT_MY_BUDGET(BAD_REQUEST, "본인의 예산만 수정/삭제할 수 있습니다."),
    BUDGET_ALREADY_EXISTS(BAD_REQUEST, "해당 날짜에 이미 등록된 예산이 있습니다. " +
            "기존 예산을 삭제하거나 적용날짜를 변경한 후 다시 시도 해주세요."),
    BUDGET_AMOUNT_TOO_SMALL(BAD_REQUEST, "최소 예산은 1000원 입니다."),
    INVALID_BUDGET_DATE(BAD_REQUEST,"시작일은 종료일보다 빠를 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
