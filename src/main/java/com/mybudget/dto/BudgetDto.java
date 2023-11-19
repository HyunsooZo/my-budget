package com.mybudget.dto;

import com.mybudget.domain.Budget;
import com.mybudget.enums.Categories;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetDto {
    private Long id;
    private Categories category;
    private BigDecimal amount;
    private Date startDate;
    private Date endDate;

    public static BudgetDto from(Budget budget) {
        return BudgetDto.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .amount(budget.getAmount())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .build();
    }
}
