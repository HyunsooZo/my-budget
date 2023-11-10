package com.mybudget.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetSettingRequestDto {
    private BigDecimal totalAmount;
    List<BudgetDto> budgets;
}
