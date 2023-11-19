package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.User;
import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.enums.UserStatus;
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

import static com.mybudget.enums.Categories.EDUCATION;
import static org.mockito.Mockito.*;

@DisplayName("지출 생성 테스트")
class ExpenseCreationTest {

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

    static ExpenseCreationRequestDto expenseCreationRequestDto =
            ExpenseCreationRequestDto.builder()
                    .excluding(false)
                    .expenseDate(new Date(2023, 1, 1))
                    .description("메모")
                    .amount(BigDecimal.valueOf(100000))
                    .category(EDUCATION)
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
    @DisplayName("성공")
    public void createExpense_success() {
        //given
        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.ofNullable(user));

        //when
        expenseService.createExpense(1L, expenseCreationRequestDto);

        //then
        verify(expenseRepository).save(any());
    }
}