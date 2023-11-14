package com.mybudget.controller;

import com.mybudget.config.JwtProvider;
import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.dto.ExpenseListResponseDto;
import com.mybudget.dto.ExpenseModificationRequestDto;
import com.mybudget.enums.Categories;
import com.mybudget.service.ExpenseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Date;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

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

    @GetMapping
    @ApiOperation(value = "지출 조회", notes = "사용자 본인의 지출을 조회")
    public ResponseEntity<ExpenseListResponseDto> getExpenses(
            @RequestParam Date startDate,
            @RequestParam Date endDate,
            @RequestParam(required = false) BigDecimal minimumAmount,
            @RequestParam(required = false) BigDecimal maximumAmount,
            @RequestParam(required = false) Categories category,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size,
            @RequestHeader(AUTHORIZATION) String token) {

        Long userId = jwtProvider.getIdFromToken(token);

        ExpenseListResponseDto expenses =
                expenseService.getExpenses(
                        userId, startDate, endDate, minimumAmount, maximumAmount, category, page, size
                );

        return ResponseEntity.status(OK).body(expenses);
    }

    @PatchMapping("/{expenseId}")
    @ApiOperation(value = "지출 수정", notes = "사용자 본인의 지출을 수정")
    public ResponseEntity<Void> updateExpense(
            @RequestHeader(AUTHORIZATION) String token,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseModificationRequestDto expenseModificationRequestDto) {

        Long userId = jwtProvider.getIdFromToken(token);

        expenseService.updateExpense(userId, expenseId, expenseModificationRequestDto);

        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/{expenseId}")
    @ApiOperation(value = "지출 삭제", notes = "사용자 본인의 지출을 삭제")
    public ResponseEntity<Void> deleteExpense(
            @RequestHeader(AUTHORIZATION) String token,
            @PathVariable Long expenseId) {

        Long userId = jwtProvider.getIdFromToken(token);

        expenseService.deleteExpense(userId, expenseId);

        return ResponseEntity.status(NO_CONTENT).build();
    }

}
