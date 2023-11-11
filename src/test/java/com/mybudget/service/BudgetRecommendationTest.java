package com.mybudget.service;

import com.mybudget.domain.CategoryRatio;
import com.mybudget.dto.BudgetDto;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.CategoryRatioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.mybudget.enums.Categories.*;
import static com.mybudget.exception.ErrorCode.BUDGET_AMOUNT_TOO_SMALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("예산 추천 테스트")
class BudgetRecommendationTest {
    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRatioRepository categoryRatioRepository;

    private BudgetRecommendationService budgetRecommendationService;

    static List<CategoryRatio> categoryRatios =
            Arrays.asList(
                    CategoryRatio.builder()
                            .category(FOOD)
                            .ratio(0.1)
                            .build(),
                    CategoryRatio.builder()
                            .category(OTHER)
                            .ratio(0.2)
                            .build(),
                    CategoryRatio.builder()
                            .category(HOUSING)
                            .ratio(0.3)
                            .build(),
                    CategoryRatio.builder()
                            .category(TRANSPORTATION)
                            .ratio(0.4)
                            .build()
            );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        budgetRecommendationService =
                new BudgetRecommendationService(budgetRepository, categoryRatioRepository);
    }

    @Test
    @DisplayName("성공")
    public void getRecommendations_success() {
        //given
        Long amount = 10000L;
        when(categoryRatioRepository.findAll()).thenReturn(categoryRatios);
        //when
        List<BudgetDto> recommendationBudgets =
                budgetRecommendationService.getRecommendationBudgets(amount);
        //then
        assertThat(recommendationBudgets.get(0).getAmount())
                .isEqualTo(BigDecimal.valueOf(1000));
        assertThat(recommendationBudgets.get(1).getAmount())
                .isEqualTo(BigDecimal.valueOf(2000));
        assertThat(recommendationBudgets.get(2).getAmount())
                .isEqualTo(BigDecimal.valueOf(3000));
        assertThat(recommendationBudgets.get(3).getAmount())
                .isEqualTo(BigDecimal.valueOf(4000));

    }

    @Test
    @DisplayName("실패 - 최소예산 미달")
    public void getRecommendations_fail_amount_too_small() {
        //given
        Long amount = 100L;
        when(categoryRatioRepository.findAll()).thenReturn(categoryRatios);
        //when&then

        assertThatThrownBy(() -> budgetRecommendationService.getRecommendationBudgets(amount))
                .isInstanceOf(CustomException.class)
                .hasMessage(BUDGET_AMOUNT_TOO_SMALL.getMessage());
    }
}