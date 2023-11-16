package com.mybudget.service;

import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("통계 서비스 테스트")
class StatisticDayOfWeekTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    private StatisticService statisticService;

    private final Long userId = 1L;
    private final Date today = Date.valueOf(LocalDate.now());
    private final DayOfWeek dayOfWeekOfToday = today.toLocalDate().getDayOfWeek();
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        statisticService = new StatisticService(expenseRepository, userRepository);
    }

    @Test
    @DisplayName("성공 - 요일기준")
    public void testStatistic_day_of_week() {
        // given
        BigDecimal mockAmountByDayOfWeek = BigDecimal.valueOf(50);
        BigDecimal mockAmountOfTodayByDayOfWeek = BigDecimal.valueOf(10);

        when(expenseRepository.getAmountAverageByDayOfWeek(userId, today, dayOfWeekOfToday))
                .thenReturn(mockAmountByDayOfWeek);
        when(expenseRepository.getAmountOfTodayByDayOfWeek(userId, today, dayOfWeekOfToday))
                .thenReturn(mockAmountOfTodayByDayOfWeek);

        // when
        Double result = statisticService.getDayOfWeekStatistics(1L, today);

        // then
        assertThat(result).isEqualTo(20.0);
    }
}
