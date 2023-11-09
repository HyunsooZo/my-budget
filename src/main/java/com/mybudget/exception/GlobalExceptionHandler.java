package com.mybudget.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static com.mybudget.exception.ErrorCode.UNDEFINED_EXCEPTION;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation 예외 처리
     *
     * @param exception MethodArgumentNotValidException 예외 객체
     * @return ResponseEntity<List<String>> 유효성 검사 오류 목록
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidException(
            MethodArgumentNotValidException exception) {

        List<String> errors = new ArrayList<>();
        exception.getBindingResult().getAllErrors()
                .forEach(error -> errors.add(error.getDefaultMessage()));

        return ResponseEntity.status(400).body(errors);
    }

    /**
     * CustomException 예외 처리
     *
     * @param exception CustomException 예외 객체
     * @return ResponseEntity<ErrorResponse> 사용자 정의 예외 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(exception.getErrorCode())
                .message(exception.getMessage())
                .build();

        log.error(exception.getMessage(), exception);

        return ResponseEntity
                .status(errorResponse.getErrorCode().getStatus())
                .body(errorResponse);
    }

    /**
     * 별도로 정의되지 않았으나 발생한 예외 처리
     *
     * @param exception Exception 예외 객체
     * @return ResponseEntity<ErrorResponse> 정의되지 않은 예외 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> undefinedException(Exception exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(exception.getMessage())
                .errorCode(UNDEFINED_EXCEPTION)
                .build();

        log.error(exception.getMessage(), exception);

        return ResponseEntity
                .status(errorResponse.getErrorCode().getStatus())
                .body(errorResponse);
    }
}
