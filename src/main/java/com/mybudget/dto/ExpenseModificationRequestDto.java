package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseModificationRequestDto {

    @Positive(message = "금액은 0보다 커야합니다")
    private BigDecimal amount;

    private String description;

    private Boolean excluding;
}
