package com.mybudget.controller;

import com.mybudget.dto.UserOtpGenerationRequestDto;
import com.mybudget.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;

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

}
