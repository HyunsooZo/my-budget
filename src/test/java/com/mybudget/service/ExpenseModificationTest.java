package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Expense;
import com.mybudget.domain.User;
import com.mybudget.dto.ExpenseModificationRequestDto;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("지출 내역 수정")
class ExpenseModificationTest {

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

    public static User user = User.builder()
            .id(1L)
            .email("email@test.com")
            .phoneNumber("112333333")
            .password("aaaaa")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.ROLE_USER)
            .build();

    @Test
    @DisplayName("성공")
    void testModifyExpense_success() {
        //given
        ExpenseModificationRequestDto expenseModificationRequestDto =
                ExpenseModificationRequestDto.builder()
                        .amount(BigDecimal.valueOf(10000))
                        .description("수정된 내용")
                        .excluding(true)
                        .build();

        Expense expense = Expense.builder()
                .id(1L)
                .user(user)
                .description("수정 전 내용")
                .category(Categories.FOOD)
                .amount(BigDecimal.valueOf(20000))
                .expenseDate(Date.valueOf("2024-01-01"))
                .excluding(false)
                .build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        //when
        expenseService.updateExpense(
                user.getId(), expense.getId(), expenseModificationRequestDto
        );

        //then
        assertThat(expense.getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(expense.getDescription()).isEqualTo("수정된 내용");
        assertThat(expense.getExcluding()).isEqualTo(true);
    }

    @Test
    @DisplayName("실패 - 본인 지출 아님")
    void testModifyExpense_fail_not_my_expense() {
        //given
        ExpenseModificationRequestDto expenseModificationRequestDto =
                ExpenseModificationRequestDto.builder()
                        .amount(BigDecimal.valueOf(10000))
                        .description("수정된 내용")
                        .excluding(true)
                        .build();

        Expense expense = Expense.builder()
                .id(1L)
                .user(User.builder().id(2L).build())
                .description("수정 전 내용")
                .category(Categories.FOOD)
                .amount(BigDecimal.valueOf(20000))
                .expenseDate(Date.valueOf("2024-01-01"))
                .excluding(false)
                .build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        //when&then
        assertThatThrownBy(() -> expenseService.updateExpense(
                user.getId(), expense.getId(), expenseModificationRequestDto
        )).isInstanceOf(CustomException.class)
                .hasMessage(NOT_MY_EXPENSE.getMessage());
    }
}