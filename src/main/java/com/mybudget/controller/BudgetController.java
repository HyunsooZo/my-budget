package com.mybudget.controller;

import com.mybudget.config.JwtProvider;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetSettingRequestDto;
import com.mybudget.dto.BudgetSettingResponseDto;
import com.mybudget.enums.Categories;
import com.mybudget.service.BudgetManagementService;
import com.mybudget.service.BudgetSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
@Api(tags = "Budget API", description = "예산과 관련된 API")
@RestController
public class BudgetController {

    private final BudgetSettingService budgetSettingService;
    private final BudgetManagementService budgetManagementService;
    private final JwtProvider jwtProvider;

    @GetMapping("/categories")
    @ApiOperation(value = "예산 카테고리 조회", notes = "예산 카테고리 목록 조회")
    public ResponseEntity<List<Categories>> getCategories(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        List<Categories> result = budgetSettingService.getCategories();

        return ResponseEntity.status(OK).body(result);
    }

    @PostMapping
    @ApiOperation(value = "예산 설정", notes = "사용자 본인의 예산을 설정")
    public ResponseEntity<Void> createBudget(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @Valid @RequestBody BudgetSettingRequestDto budgetSettingRequestDto){

        Long userId = jwtProvider.getIdFromToken(token);

        budgetSettingService.createBudget(userId, budgetSettingRequestDto);

        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    @ApiOperation(value = "예산 설정 조회", notes = "사용자 본인의 예산 설정 조회")
    public ResponseEntity<BudgetSettingResponseDto> getMyBudgets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        Long userId = jwtProvider.getIdFromToken(token);

        List<BudgetDto> result = budgetManagementService.getMyBudgets(userId);

        return ResponseEntity.status(OK).body(BudgetSettingResponseDto.from(result));
    }
}
