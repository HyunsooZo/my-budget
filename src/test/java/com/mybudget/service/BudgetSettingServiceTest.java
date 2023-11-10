package com.mybudget.service;

import com.mybudget.config.UserRole;
import com.mybudget.domain.Budget;
import com.mybudget.domain.CategoryRatio;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetSettingRequestDto;
import com.mybudget.enums.UserStatus;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.CategoryRatioRepository;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.mybudget.enums.Categories.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("예산 설정 테스트")
class BudgetSettingServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRatioRepository categoryRatioRepository;

    private BudgetSettingService budgetSettingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        budgetSettingService = new BudgetSettingService(
                userRepository, budgetRepository, categoryRatioRepository
        );
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
                    .totalAmount(BigDecimal.valueOf(1000000))
                    .budgets(budgetDtos)
                    .build();

    static BudgetSettingRequestDto budgetSettingRequestDtoWOList =
            BudgetSettingRequestDto.builder()
                    .totalAmount(BigDecimal.valueOf(1000000))
                    .budgets(new ArrayList<>())
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
        budgetSettingService.createBudget(userId, budgetSettingRequestDto);

        // then
        assertEquals(4, budgetDtos.size());
        verify(budgetRepository, Mockito.times(4))
                .save(Mockito.any(Budget.class));
    }

    @Test
    @DisplayName("성공 - 예산 계획 추천")
    public void createBudget_success_w_no_list() {
        // given
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.findByUser(user)).thenReturn(new ArrayList<>());
        when(categoryRatioRepository.findAll())
                .thenReturn(Arrays.asList(
                        CategoryRatio.builder()
                                .category(FOOD)
                                .ratio(10.0)
                                .build(),
                        CategoryRatio.builder()
                                .category(OTHER)
                                .ratio(20.0)
                                .build(),
                        CategoryRatio.builder()
                                .category(HOUSING)
                                .ratio(20.0)
                                .build(),
                        CategoryRatio.builder()
                                .category(TRANSPORTATION)
                                .ratio(50.0)
                                .build()
                ));
        when(categoryRatioRepository.findByCategory(FOOD))
                .thenReturn(Optional.of(
                        CategoryRatio.builder()
                                .category(FOOD)
                                .ratio(10.0)
                                .build()
                ));
        when(categoryRatioRepository.findByCategory(OTHER))
                .thenReturn(Optional.of(
                        CategoryRatio.builder()
                                .category(OTHER)
                                .ratio(20.0)
                                .build()
                ));
        when(categoryRatioRepository.findByCategory(HOUSING))
                .thenReturn(Optional.of(
                        CategoryRatio.builder()
                                .category(HOUSING)
                                .ratio(20.0)
                                .build()
                ));
        when(categoryRatioRepository.findByCategory(TRANSPORTATION))
                .thenReturn(Optional.of(
                        CategoryRatio.builder()
                                .category(TRANSPORTATION)
                                .ratio(50.0)
                                .build()
                ));
        when(budgetRepository.findByUserAndCategory(user, FOOD))
                .thenReturn(Optional.empty());
        when(budgetRepository.findByUserAndCategory(user, OTHER))
                .thenReturn(Optional.empty());
        when(budgetRepository.findByUserAndCategory(user, HOUSING))
                .thenReturn(Optional.empty());
        when(budgetRepository.findByUserAndCategory(user, TRANSPORTATION))
                .thenReturn(Optional.empty());

        // when
        budgetSettingService.createBudget(userId, budgetSettingRequestDtoWOList);

        // then
        assertEquals(4, budgetDtos.size());
        verify(budgetRepository, Mockito.times(4))
                .save(Mockito.any(Budget.class));    }
}