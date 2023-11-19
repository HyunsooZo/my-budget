package com.mybudget.dto;

import com.mybudget.enums.Categories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.sql.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseCreationRequestDto {
    @NotNull(message = "지출 날짜를 입력해주세요")
    private Date expenseDate;

    @NotNull(message = "카테고리를 입력해주세요")
    private Categories category;

    @NotNull(message = "금액을 입력해주세요")
    @Positive(message = "금액은 0보다 커야합니다")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "지출을 제외할지 여부를 입력해주세요")
    private Boolean excluding;
}
