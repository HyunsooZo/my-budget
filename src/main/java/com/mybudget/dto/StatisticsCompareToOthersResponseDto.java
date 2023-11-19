package com.mybudget.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsCompareToOthersResponseDto {
    private Double ratioCompareToOthers;
}
