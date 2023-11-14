package com.mybudget.service;

import com.mybudget.domain.Expense;
import com.mybudget.domain.User;
import com.mybudget.dto.AmountsOfCategoryDto;
import com.mybudget.dto.ExpenseListResponseDto;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("지출 내역 조회")
class ExpenseInquiryTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    private ExpenseService expenseService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        expenseService = new ExpenseService(expenseRepository, userRepository);
    }

    @Test
    @DisplayName("성공")
    void testGetExpenses_success() {
        // given
        Date startDate = Date.valueOf("2023-01-01");
        Date endDate = Date.valueOf("2023-12-31");
        BigDecimal minimumAmount = BigDecimal.valueOf(100);
        BigDecimal maximumAmount = BigDecimal.valueOf(500);
        Categories category = Categories.FOOD;
        Integer page = 0;
        Integer size = 10;
        BigDecimal totalAmount = BigDecimal.valueOf(200);
        User user = User.builder().id(1L).build();

        Expense expense1 = Expense.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .category(Categories.FOOD)
                .expenseDate(Date.valueOf("2023-01-01"))
                .excluding(true)
                .description(null)
                .user(User.builder().id(1L).build())
                .build();

        Expense expense2 = Expense.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(200))
                .category(Categories.FOOD)
                .expenseDate(Date.valueOf("2023-01-02"))
                .excluding(false)
                .description(null)
                .user(User.builder().id(1L).build())
                .build();

        Expense expense3 = Expense.builder()
                .id(3L)
                .amount(BigDecimal.valueOf(200))
                .category(Categories.EDUCATION)
                .expenseDate(Date.valueOf("2023-01-02"))
                .excluding(false)
                .description(null)
                .user(User.builder().id(1L).build())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<Expense> expenses = Arrays.asList(expense1, expense2, expense3);

        when(expenseRepository.getExpensesByPeriodWithCategory(
                user.getId(), startDate, endDate, category, minimumAmount, maximumAmount))
                .thenReturn(expenses);

        ExpenseListResponseDto expenseListResponseDto =
                ExpenseListResponseDto.builder()
                        .totalAmount(BigDecimal.valueOf(200))
                        .amountsPerCategory(
                                Collections.singletonList(AmountsOfCategoryDto.builder()
                                        .category(Categories.FOOD)
                                        .totalAmount(BigDecimal.valueOf(200)).build()
                                )
                        )
                        .build();
        List<AmountsOfCategoryDto> amountsOfCategoryDtos =
                Collections.singletonList(AmountsOfCategoryDto.from(expense2));

        when(expenseRepository.getAmountsByPeriodWithCategory(
                user.getId(), startDate, endDate, category, minimumAmount, maximumAmount))
                .thenReturn(amountsOfCategoryDtos);

        when(expenseRepository.getTotalAmountByPeriodWithCategory(
                user.getId(), startDate, endDate, category, minimumAmount, maximumAmount))
                .thenReturn(totalAmount);
        // when
        ExpenseListResponseDto response = expenseService.getExpenses(
                user.getId(), startDate, endDate, minimumAmount, maximumAmount, category, page, size);

        // then
        assertThat(response.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(response.getAmountsPerCategory().get(0).getCategory())
                .isEqualTo(expenseListResponseDto.getAmountsPerCategory().get(0).getCategory());
    }
}