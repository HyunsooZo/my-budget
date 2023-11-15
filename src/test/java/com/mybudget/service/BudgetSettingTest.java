package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetSettingRequestDto;
import com.mybudget.enums.UserStatus;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.mybudget.enums.Categories.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("예산 설정 테스트")
class BudgetSettingTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    private BudgetService budgetService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        budgetService = new BudgetService(userRepository, budgetRepository);
    }

    static List<BudgetDto> budgetDtos = Arrays.asList(
            BudgetDto.builder()
                    .category(FOOD)
                    .amount(BigDecimal.valueOf(100000))
                    .startDate(new Date(2023, 1, 1))
                    .endDate(new Date(2023, 2, 1))
                    .build(),
            BudgetDto.builder()
                    .category(OTHER)
                    .amount(BigDecimal.valueOf(200000))
                    .startDate(new Date(2023, 1, 1))
                    .endDate(new Date(2023, 2, 1))
                    .build(),
            BudgetDto.builder()
                    .category(HOUSING)
                    .amount(BigDecimal.valueOf(300000))
                    .startDate(new Date(2023, 1, 1))
                    .endDate(new Date(2023, 2, 1))
                    .build(),
            BudgetDto.builder()
                    .category(TRANSPORTATION)
                    .amount(BigDecimal.valueOf(400000))
                    .startDate(new Date(2023, 1, 1))
                    .endDate(new Date(2023, 2, 1))
                    .build()
    );
    static BudgetSettingRequestDto budgetSettingRequestDto =
            BudgetSettingRequestDto.builder()
                    .budgets(budgetDtos)
                    .build();

    static User user = User.builder()
            .id(1L)
            .email("email@test.com")
            .phoneNumber("112333333")
            .password("aaaaa")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.ROLE_USER)
            .build();

    static Budget budget = Budget.builder()
            .id(2L)
            .category(FOOD)
            .amount(BigDecimal.valueOf(400000))
            .startDate(new Date(2023, 1, 31))
            .endDate(new Date(2023, 2, 10))
            .build();

    @Test
    @DisplayName("성공 - 일반")
    public void createBudget_success_normal() {
        // given
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findByUser(user))
                .thenReturn(new ArrayList<>());

        // when
        budgetService.createBudget(userId, budgetSettingRequestDto);

        // then
        assertEquals(4, budgetDtos.size());
        verify(budgetRepository, Mockito.times(4))
                .save(Mockito.any(Budget.class));
    }
}