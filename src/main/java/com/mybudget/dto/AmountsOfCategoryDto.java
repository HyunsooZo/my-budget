package com.mybudget.dto;

import com.mybudget.domain.Expense;
import com.mybudget.enums.Categories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmountsOfCategoryDto {
    private Categories category;
    private BigDecimal totalAmount;

    public static AmountsOfCategoryDto from(Expense expense) {
        return AmountsOfCategoryDto.builder()
                .category(expense.getCategory())
                .totalAmount(expense.getAmount())
                .build();
    }
}
