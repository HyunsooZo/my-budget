package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRecommendationRequestDto {
    private BigDecimal totalBudget;
}
