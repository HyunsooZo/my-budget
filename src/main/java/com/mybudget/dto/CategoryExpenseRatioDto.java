package com.mybudget.dto;

import com.mybudget.enums.Categories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryExpenseRatioDto {

    private Categories categories;
    private Double ratio;

    public static CategoryExpenseRatioDto from(Categories key, BigDecimal percentageIncrease) {
        return CategoryExpenseRatioDto.builder()
                .categories(key)
                .ratio(percentageIncrease.doubleValue())
                .build();
    }
}
