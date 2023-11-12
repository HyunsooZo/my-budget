package com.mybudget.controller;

import com.mybudget.config.JwtProvider;
import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.service.ExpenseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RequestMapping("/api/v1/expenses")
@Api(tags = "Expense API", description = "지출과 관련된 API")
@RestController
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtProvider jwtProvider;

    @PostMapping
    @ApiOperation(value = "지출 등록", notes = "사용자 본인의 지출을 등록")
    public ResponseEntity<Void> createExpense(
            @RequestHeader(AUTHORIZATION) String token,
            @Valid @RequestBody ExpenseCreationRequestDto expenseCreationRequestDto) {

        Long userId = jwtProvider.getIdFromToken(token);

        expenseService.createExpense(userId, expenseCreationRequestDto);

        return ResponseEntity.status(CREATED).build();
    }
}
