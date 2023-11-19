package com.mybudget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticByDayOfWeekResponseDto {
    private Double rateCompareToOtherDaysOfWeek;

    public static StatisticByDayOfWeekResponseDto from(Double rateCompareToOtherDaysOfWeek) {
        return StatisticByDayOfWeekResponseDto.builder()
                .rateCompareToOtherDaysOfWeek(rateCompareToOtherDaysOfWeek)
                .build();
    }
}
