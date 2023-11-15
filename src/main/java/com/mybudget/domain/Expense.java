package com.mybudget.domain;

import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.enums.Categories;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Expense extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Setter
    private String description;

    private Categories category;

    @Setter
    private BigDecimal amount;

    private Date expenseDate;

    private BigDecimal budgetTotalAmount;

    @Setter
    private Boolean excluding;

    public static Expense from(User user,
                               ExpenseCreationRequestDto expenseCreationRequestDto,
                               BigDecimal budgetTotalAmount) {
        return Expense.builder()
                .user(user)
                .description(expenseCreationRequestDto.getDescription())
                .category(expenseCreationRequestDto.getCategory())
                .amount(expenseCreationRequestDto.getAmount())
                .excluding(expenseCreationRequestDto.getExcluding())
                .expenseDate(expenseCreationRequestDto.getExpenseDate())
                .budgetTotalAmount(budgetTotalAmount)
                .build();
    }
}
