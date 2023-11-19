package com.mybudget.dto;

import com.mybudget.domain.Expense;
import com.mybudget.enums.Categories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDto {
    private Long id;

    private String description;

    private Categories category;

    private BigDecimal amount;

    private Date expenseDate;

    private Boolean excluding;

    public static ExpenseDto from(Expense expense) {
        return ExpenseDto.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .excluding(expense.getExcluding())
                .build();
    }
}
