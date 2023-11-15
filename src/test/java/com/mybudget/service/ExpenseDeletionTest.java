package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Expense;
import com.mybudget.domain.User;
import com.mybudget.enums.Categories;
import com.mybudget.enums.UserStatus;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;

import static com.mybudget.exception.ErrorCode.NOT_MY_EXPENSE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("지출 내역 삭제")
class ExpenseDeletionTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    private ExpenseService expenseService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        expenseService = new ExpenseService(
                expenseRepository, budgetRepository, userRepository
        );
    }


    static User user = User.builder()
            .id(1L)
            .email("email@test.com")
            .phoneNumber("112333333")
            .password("aaaaa")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.ROLE_USER)
            .build();

    static Expense expense = Expense.builder()
            .id(1L)
            .user(user)
            .description("수정 전 내용")
            .category(Categories.FOOD)
            .amount(BigDecimal.valueOf(20000))
            .expenseDate(Date.valueOf("2024-01-01"))
            .excluding(false)
            .build();

    @Test
    @DisplayName("성공")
    void testDeleteExpense_success() {
        //given
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //when
        expenseService.deleteExpense(user.getId(), expense.getId());
        //then
        verify(expenseRepository, times(1)).delete(expense);

    }

    @Test
    @DisplayName("실패 - 본인 지출 아님")
    void testDeleteExpense_fail_not_my_expense() {
        //given
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(User.builder().id(2L).build()));

        //when&then
        assertThatThrownBy(() -> expenseService.deleteExpense(user.getId(), expense.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_MY_EXPENSE.getMessage());
    }
}