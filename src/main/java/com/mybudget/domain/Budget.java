package com.mybudget.domain;

import com.mybudget.dto.BudgetDto;
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
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private Categories category;

    @Setter
    private BigDecimal amount;

    private Date startDate;

    private Date endDate;

    public static Budget from(User user, BudgetDto budgetDto) {
        return Budget.builder()
                .user(user)
                .category(budgetDto.getCategory())
                .amount(budgetDto.getAmount())
                .startDate(budgetDto.getStartDate())
                .endDate(budgetDto.getEndDate())
                .build();
    }
}
