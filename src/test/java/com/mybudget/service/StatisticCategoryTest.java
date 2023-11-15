package com.mybudget.service;

import com.mybudget.domain.Expense;
import com.mybudget.dto.CategoryExpenseRatioDto;
import com.mybudget.enums.Categories;
import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("통계 서비스 테스트")
class StatisticCategoryTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    private StatisticService statisticService;

    private final Long userId = 1L;
    private final Date today = Date.valueOf(LocalDate.now());
    private final Date thisMonthStartDate = Date.valueOf(today.toLocalDate().minusMonths(1));
    private final Date lastMonthStartDate = Date.valueOf(today.toLocalDate().minusMonths(2));
    private final Date lastMonthEndDate = Date.valueOf(today.toLocalDate().minusMonths(1).minusDays(1));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        statisticService = new StatisticService(expenseRepository, userRepository);
    }
    @Test
    @DisplayName("성공 - 카테고리별")
    void testGetCategoryStatistics_success() {
        //given
        Expense expense1 = Expense.builder()
                .id(1L)
                .user(null)
                .description("test")
                .category(Categories.FOOD)
                .amount(BigDecimal.valueOf(4000.00))
                .excluding(false)
                .expenseDate(today)
                .dayOfWeek(today.toLocalDate().getDayOfWeek())
                .expenseRatio(50.00)
                .budgetTotalAmount(BigDecimal.valueOf(8000.00))
                .build();
        Expense expense2 = Expense.builder()
                .id(2L)
                .user(null)
                .description("test")
                .category(Categories.EDUCATION)
                .amount(BigDecimal.valueOf(4000.00))
                .excluding(false)
                .expenseDate(today)
                .dayOfWeek(today.toLocalDate().getDayOfWeek())
                .expenseRatio(50.00)
                .budgetTotalAmount(BigDecimal.valueOf(8000.00))
                .build();

        List<Expense> thisMonthExpenses = Arrays.asList(expense1, expense2);

        Expense expense3 = Expense.builder()
                .id(3L)
                .user(null)
                .description("test")
                .category(Categories.FOOD)
                .amount(BigDecimal.valueOf(3000.00))
                .excluding(false)
                .expenseDate(Date.valueOf(today.toLocalDate().minusMonths(2)))
                .dayOfWeek(today.toLocalDate().getDayOfWeek())
                .expenseRatio(50.00)
                .budgetTotalAmount(BigDecimal.valueOf(3000.00))
                .build();
        Expense expense4 = Expense.builder()
                .id(4L)
                .user(null)
                .description("test")
                .category(Categories.EDUCATION)
                .amount(BigDecimal.valueOf(3000.00))
                .excluding(false)
                .expenseDate(Date.valueOf(today.toLocalDate().minusMonths(2)))
                .dayOfWeek(today.toLocalDate().getDayOfWeek())
                .expenseRatio(50.00)
                .budgetTotalAmount(BigDecimal.valueOf(3000.00))
                .build();

        List<Expense> lastMonthExpenses = Arrays.asList(expense3, expense4);

        when(expenseRepository.getExpensesByMonth(userId, lastMonthStartDate, lastMonthEndDate))
                .thenReturn(lastMonthExpenses);
        when(expenseRepository.getExpensesByMonth(userId, thisMonthStartDate, today))
                .thenReturn(thisMonthExpenses);

        //when
        List<CategoryExpenseRatioDto> result = statisticService.getCategoryStatistics(userId, today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getRatio()).isEqualTo(133.00);
    }
}
