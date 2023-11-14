package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseListResponseDto {
    private Page<ExpenseDto> expenses;
    private BigDecimal totalAmount;
    private List<AmountsOfCategoryDto> amountsPerCategory;
}
