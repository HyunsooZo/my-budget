package com.mybudget.domain;

import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.enums.Categories;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.DayOfWeek;

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

    private DayOfWeek dayOfWeek;

    private Double expenseRatio;

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
                .dayOfWeek(expenseCreationRequestDto.getExpenseDate().toLocalDate().getDayOfWeek())
                .expenseRatio(expenseCreationRequestDto.getAmount()
                        .divide(budgetTotalAmount, 2,
                                RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue())
                .budgetTotalAmount(budgetTotalAmount)
                .build();
    }
}
