package com.mybudget.domain;

import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.enums.Categories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.sql.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Expense extends BaseEntity{
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String description;

    private Categories category;

    private BigDecimal amount;

    private Date expenseDate;

    private Boolean excluding;

    public static Expense from(User user,
                               ExpenseCreationRequestDto expenseCreationRequestDto) {
        return Expense.builder()
                .user(user)
                .description(expenseCreationRequestDto.getDescription())
                .category(expenseCreationRequestDto.getCategory())
                .amount(expenseCreationRequestDto.getAmount())
                .excluding(expenseCreationRequestDto.getExcluding())
                .expenseDate(expenseCreationRequestDto.getExpenseDate())
                .build();
    }
}
