package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetEditRequestDto;
import com.mybudget.enums.UserStatus;
import com.mybudget.exception.CustomException;
import com.mybudget.exception.ErrorCode;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static com.mybudget.enums.Categories.FOOD;
import static com.mybudget.exception.ErrorCode.BUDGET_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("예산 수정 테스트")
class BudgetEditTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    private BudgetService budgetService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        budgetService = new BudgetService(
                userRepository, budgetRepository
        );
    }

    static BudgetEditRequestDto budgetEditRequestDto = BudgetEditRequestDto.builder()
            .amount(BigDecimal.valueOf(99))
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
            .id(1L)
            .user(user)
            .category(FOOD)
            .amount(BigDecimal.valueOf(100000))
            .build();

    static Budget budget2 = Budget.builder()
            .id(1L)
            .user(new User())
            .category(FOOD)
            .amount(BigDecimal.valueOf(100000))
            .build();


    @Test
    @DisplayName("성공")
    void editBudget_success() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        //when
        budgetService.editBudget(1L, 1L, budgetEditRequestDto);

        //then
        assertThat(budget.getAmount()).isEqualTo(BigDecimal.valueOf(99));
    }

    @Test
    @DisplayName("실패 - 예산정보없음")
    void editBudget_budget_not_found() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() ->
                budgetService.editBudget(1L, 1L, budgetEditRequestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(BUDGET_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("실패 - 본인 예산 아님")
    void editBudget_not_my_budget() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget2));

        //when&then
        assertThatThrownBy(() ->
                budgetService.editBudget(1L, 1L, budgetEditRequestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NOT_MY_BUDGET.getMessage());
    }

}