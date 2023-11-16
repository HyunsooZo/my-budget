package com.mybudget.controller;

import com.mybudget.config.JwtProvider;
import com.mybudget.dto.CategoryExpenseRatioDto;
import com.mybudget.dto.StatisticByCategoryResponseDto;
import com.mybudget.dto.StatisticByDayOfWeekResponseDto;
import com.mybudget.service.StatisticService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
@Api(tags = "Statistic API", description = "통계와 관련된 API")
@RestController
public class StatisticController {

    private final StatisticService statisticService;
    private final JwtProvider jwtProvider;

    @GetMapping("/category")
    @ApiOperation(value = "통계 조회", notes = "통계를 조회")
    public ResponseEntity<StatisticByCategoryResponseDto> getStatistics(
            @RequestHeader(AUTHORIZATION) String token) {

        Long userId = jwtProvider.getIdFromToken(token);


        List<CategoryExpenseRatioDto> categoryStatistics =
                statisticService.getCategoryStatistics(
                        userId, Date.valueOf(LocalDate.now())
                );

        StatisticByCategoryResponseDto statistics = StatisticByCategoryResponseDto.builder()
                .ExpenseRatiosPerCategory(categoryStatistics)
                .build();

        return ResponseEntity.status(OK).body(statistics);
    }

    @GetMapping("/day-of-week")
    @ApiOperation(value = "지난요일 대비 통계 조회", notes = "지난요일 대비 통계를 조회")
    public ResponseEntity<StatisticByDayOfWeekResponseDto> getStatisticsByDayOfWeek(
            @RequestHeader(AUTHORIZATION) String token) {

        Long userId = jwtProvider.getIdFromToken(token);

        Double dayOfWeekStatistics =
                statisticService.getDayOfWeekStatistics(userId, Date.valueOf(LocalDate.now()));

        return ResponseEntity.status(OK)
                .body(StatisticByDayOfWeekResponseDto.from(dayOfWeekStatistics));
    }
}
