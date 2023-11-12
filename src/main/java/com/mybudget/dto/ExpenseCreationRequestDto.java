package com.mybudget.dto;

import com.mybudget.enums.Categories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.sql.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseCreationRequestDto {
    @NotBlank(message = "지출일을 입력해주세요")
    private Date expenseDate;

    @NotBlank(message = "카테고리를 입력해주세요")
    private Categories category;

    @NotBlank(message = "금액을 입력해주세요")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "지출 통계 제외 여부를 입력해주세요")
    private Boolean excluding;
}
