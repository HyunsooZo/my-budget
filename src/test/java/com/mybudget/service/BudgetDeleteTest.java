package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
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
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mybudget.enums.Categories.FOOD;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("예산 삭제 테스트")
class BudgetDeleteTest {
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

    public static User user = User.builder()
            .id(1L)
            .email("email@test.com")
            .phoneNumber("112333333")
            .password("aaaaa")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.ROLE_USER)
            .build();
    public static User user2 = User.builder()
            .id(2L)
            .email("email@test.com")
            .phoneNumber("112333333")
            .password("aaaaa")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.ROLE_USER)
            .build();

    static Budget budget = Budget.builder()
            .id(1L)
            .user(user)
            .category(FOOD)
            .amount(BigDecimal.valueOf(100000))
            .build();
    static Budget budget2 = Budget.builder()
            .id(1L)
            .user(user2)
            .category(FOOD)
            .amount(BigDecimal.valueOf(100000))
            .build();

    @Test
    @DisplayName("성공")
    public void deleteBudget_success() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findById(budget.getId()))
                .thenReturn(Optional.of(budget));
        // when
        budgetService.deleteBudget(user.getId(), budget.getId());
        // then
        verify(budgetRepository).delete(budget);
    }

    @Test
    @DisplayName("실패 - 권한없음")
    public void deleteBudget_fail_not_my_budget() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findById(budget2.getId()))
                .thenReturn(Optional.of(budget2));
        // when&then
        Assertions.assertThatThrownBy(() -> budgetService.deleteBudget(
                        user.getId(), budget2.getId())
                )
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NOT_MY_BUDGET.getMessage());
    }

    @Test
    @DisplayName("실패 - 없는예산")
    public void deleteBudget_fail_budget_doesnt_exists() {
        // given
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(budgetRepository.findById(budget.getId()))
                .thenReturn(Optional.empty());
        // when&then
        Assertions.assertThatThrownBy(() -> budgetService.deleteBudget(
                        user.getId(), budget.getId())
                )
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.BUDGET_NOT_FOUND.getMessage());
    }

}