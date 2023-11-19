package com.mybudget.service;

import com.mybudget.domain.Expense;
import com.mybudget.domain.User;
import com.mybudget.enums.Categories;
import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@DisplayName("통계 서비스 테스트")
class StatisticComparingTest {

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
    @DisplayName("성공 - 다른 사용자와 비교")
    public void testStatistic_others_success() {
        // Given

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .category(Categories.FOOD)
                .expenseDate(today)
                .excluding(true)
                .description(null)
                .user(User.builder().id(1L).build())
                .excluding(false)
                .expenseRatio(50.0)
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(100))
                .category(Categories.FOOD)
                .expenseDate(today)
                .excluding(true)
                .description(null)
                .user(User.builder().id(2L).build())
                .excluding(false)
                .expenseRatio(80.0)
                .build();

        Expense expense3 = Expense.builder()
                .id(3L)
                .amount(BigDecimal.valueOf(100))
                .category(Categories.FOOD)
                .expenseDate(today)
                .excluding(true)
                .description(null)
                .user(User.builder().id(2L).build())
                .excluding(false)
                .expenseRatio(50.0)
                .build();

        List<Expense> allExpenses = Arrays.asList(expense1, expense2, expense3);

        when(expenseRepository.findAll()).thenReturn(allExpenses);

        // When
        Double result = statisticService.getOthersStatistics(userId, today);

        // Then
        assertThat(result).isEqualTo(76.9); // 예상되는 결과값
    }
}
