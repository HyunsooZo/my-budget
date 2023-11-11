package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetSettingRequestDto;
import com.mybudget.enums.UserStatus;
import com.mybudget.exception.CustomException;
import com.mybudget.exception.ErrorCode;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

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
                    .build(),
            BudgetDto.builder()
                    .category(OTHER)
                    .amount(BigDecimal.valueOf(200000))
                    .build(),
            BudgetDto.builder()
                    .category(HOUSING)
                    .amount(BigDecimal.valueOf(300000))
                    .build(),
            BudgetDto.builder()
                    .category(TRANSPORTATION)
                    .amount(BigDecimal.valueOf(400000))
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

    @Test
    @DisplayName("실패 - 이미 등록시도한 예산이 등록되어 있는 경우")
    public void createBudget_fail_existing_budget() {
        // given
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findByUser(user))
                .thenReturn(Collections.singletonList(new Budget()));
        when(budgetRepository.findByUserAndCategory(user, FOOD))
                .thenReturn(Optional.of(new Budget()));


        // when&then
        Assertions.assertThatThrownBy(() -> budgetService.createBudget(userId, budgetSettingRequestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.BUDGET_ALREADY_EXISTS.getMessage());

    }
}