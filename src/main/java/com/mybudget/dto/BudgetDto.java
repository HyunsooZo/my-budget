package com.mybudget.dto;

import com.mybudget.domain.Budget;
import com.mybudget.enums.Categories;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetDto {
    private Categories category;
    private BigDecimal amount;

    public static BudgetDto from(Budget budget) {
        return BudgetDto.builder()
                .category(budget.getCategory())
                .amount(budget.getAmount())
                .build();
    }
}
