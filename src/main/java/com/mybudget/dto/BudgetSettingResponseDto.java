package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetSettingResponseDto {
    private List<BudgetDto> budgets;

    public static BudgetSettingResponseDto from(List<BudgetDto> result) {
        return BudgetSettingResponseDto.builder()
                .budgets(result)
                .build();
    }
}
