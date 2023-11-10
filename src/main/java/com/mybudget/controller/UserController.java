package com.mybudget.controller;

import com.mybudget.dto.UserOtpGenerationRequestDto;
import com.mybudget.dto.UserOtpVerificationRequestDto;
import com.mybudget.dto.UserSignUpRequestDto;
import com.mybudget.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Api(tags = "User API", description = "사용자와 관련된 API")
@RestController
public class UserController {

    private final UserService userService;


    @PostMapping("/otp/request")
    @ApiOperation(value = "OTP 발송", notes = "OTP를 문자로 발송")
    public ResponseEntity<Void> sendOtp(
            @Valid @RequestBody UserOtpGenerationRequestDto userOtpGenerationRequestDto) {

        userService.sendOtp(userOtpGenerationRequestDto);

        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/otp/verification")
    @ApiOperation(value = "OTP 인증", notes = "OTP를 입력하여 휴대전화 인증")
    public ResponseEntity<Void> verifyOtp(
            @Valid @RequestBody UserOtpVerificationRequestDto userOtpVerificationRequestDto) {

        userService.verifyOtp(userOtpVerificationRequestDto);

        return ResponseEntity.status(OK).build();
    }

    @PostMapping
    @ApiOperation(value = "사용자 회원가입", notes = "사용자 회원가입 진행")
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody UserSignUpRequestDto userSignUpRequestDto) {

        userService.signUp(userSignUpRequestDto);

        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/{userId}/verification")
    @ApiOperation(value = "사용자 이메일 인증", notes = "사용자 이메일 인증(이메일로 전송된 링크)")
    public String verifyEmail(@PathVariable Long userId) {

        return userService.verifyEmail(userId);
    }

}
