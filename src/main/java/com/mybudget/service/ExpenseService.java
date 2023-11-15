package com.mybudget.service;

import com.mybudget.domain.Budget;
import com.mybudget.domain.Expense;
import com.mybudget.domain.User;
import com.mybudget.dto.*;
import com.mybudget.enums.Categories;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.mybudget.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 지출 내역 생성
     *
     * @param userId                    사용자 식별자
     * @param expenseCreationRequestDto 지출 생성 요청 DTO
     * @throws CustomException 사용자 정보를 찾을 수 없는 경우 예외를 발생시킵니다.
     */
    @Transactional
    public void createExpense(Long userId, ExpenseCreationRequestDto expenseCreationRequestDto) {

        User user = getUser(userId);

        List<Budget> budgets = budgetRepository.findByUserAndDate(
                user, expenseCreationRequestDto.getExpenseDate()
        );

        BigDecimal budgetTotalAmount = budgets.stream()
                .filter(budget -> budget.getCategory().equals(expenseCreationRequestDto.getCategory()))
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        expenseRepository.save(
                Expense.from(user, expenseCreationRequestDto, budgetTotalAmount)
        );
    }

    /**
     * 주어진 사용자 식별자에 해당하는 사용자 조회
     *
     * @param userId 사용자 식별자
     * @return 주어진 식별자에 해당하는 사용자
     * @throws CustomException 사용자 정보를 찾을 수 없는 경우 예외를 발생시킵니다.
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_INFO_NOT_FOUND));
    }

    /**
     * 지정된 조건에 따라 지출 목록과 해당 기간 동안의 카테고리별 지출 금액 반환
     *
     * @param userId        사용자 ID
     * @param startDate     조회 시작일
     * @param endDate       조회 종료일
     * @param minimumAmount 최소 금액 필터링을 위한 Optional 매개변수
     * @param maximumAmount 최대 금액 필터링을 위한 Optional 매개변수
     * @param page          페이지 번호
     * @param size          페이지 크기
     * @return 지출 목록과 해당 기간 동안의 카테고리별 지출 금액을 포함한 응답 DTO
     */
    @Transactional(readOnly = true)
    public ExpenseListResponseDto getExpenses(Long userId,
                                              Date startDate,
                                              Date endDate,
                                              BigDecimal minimumAmount,
                                              BigDecimal maximumAmount,
                                              Categories category,
                                              Integer page,
                                              Integer size) {

        // 최대 및 최소 금액을 설정합니다. 값이 없는 경우 기본값은 Long.MAX_VALUE 및 Long.MIN_VALUE입니다.
        BigDecimal minAmount = minimumAmount == null ? BigDecimal.ZERO : minimumAmount;
        BigDecimal maxAmount = maximumAmount == null ? BigDecimal.valueOf(1000000000L) : maximumAmount;

        // 지출 목록을 가져옵니다. 지출을 ExpenseDto로 매핑한 후 리스트로 변환합니다.
        List<ExpenseDto> expenses = (
                category == null ?
                        expenseRepository.getExpensesByPeriod(
                                userId, startDate, endDate, minAmount, maxAmount) :
                        expenseRepository.getExpensesByPeriodWithCategory(
                                userId, startDate, endDate, category, minAmount, maxAmount)
        ).stream().map(ExpenseDto::from).collect(Collectors.toList());

        // 카테고리별 금액을 가져옵니다.
        List<AmountsOfCategoryDto> amountsPerCategories = category == null ?
                expenseRepository.getAmountsByPeriod(
                        userId, startDate, endDate, minAmount, maxAmount) :
                expenseRepository.getAmountsByPeriodWithCategory(
                        userId, startDate, endDate, category, minAmount, maxAmount);

        // 지정된 기간 동안의 총 지출 금액을 가져옵니다.
        BigDecimal totalAmount = category == null ?
                expenseRepository.getTotalAmountByPeriod(
                        userId, startDate, endDate, minAmount, maxAmount) :
                expenseRepository.getTotalAmountByPeriodWithCategory(
                        userId, startDate, endDate, category, minAmount, maxAmount
                );


        // 페이지네이션을 설정합니다.
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        // 가져온 지출 목록으로 페이지를 생성합니다.
        Page<ExpenseDto> expenseDtoPage = new PageImpl<>(expenses, pageable, expenses.size());


        // 지출 목록, 카테고리별 금액, 총 지출 금액을 포함하는 응답 DTO를 생성하여 반환합니다.
        return ExpenseListResponseDto.builder()
                .expenses(expenseDtoPage)
                .amountsPerCategory(amountsPerCategories)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * 지출 정보를 업데이트하는 메서드입니다.
     *
     * @param userId                        사용자 ID
     * @param expenseId                     지출 ID
     * @param expenseModificationRequestDto 지출 수정 요청 DTO 객체
     */
    @Transactional
    public void updateExpense(Long userId,
                              Long expenseId,
                              ExpenseModificationRequestDto expenseModificationRequestDto) {

        // 지출 및 사용자 정보 가져오기
        Expense expense = getExpense(expenseId);
        User user = getUser(userId);

        // 지출 소유자 확인
        if (!expense.getUser().equals(user)) {
            throw new CustomException(NOT_MY_EXPENSE);
        }

        // 수정 요청에 따라 지출 정보 업데이트
        if (expenseModificationRequestDto.getDescription() != null) {
            expense.setDescription(expenseModificationRequestDto.getDescription());
        }

        if (expenseModificationRequestDto.getAmount() != null) {
            expense.setAmount(expenseModificationRequestDto.getAmount());
        }

        if (expenseModificationRequestDto.getExcluding() != null) {
            expense.setExcluding(expenseModificationRequestDto.getExcluding());
        }
    }

    /**
     * 지정된 ID에 해당하는 지출 정보를 조회하는 메서드입니다.
     *
     * @param expenseId 조회할 지출의 ID
     * @return 조회된 지출 정보
     * @throws CustomException 지출을 찾을 수 없는 경우 예외 발생
     */
    private Expense getExpense(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException(EXPENSE_NOT_FOUND));
    }

    /**
     * 지정된 사용자의 지출을 삭제하는 메서드입니다.
     *
     * @param userId    사용자 ID
     * @param expenseId 삭제할 지출의 ID
     * @throws CustomException 사용자가 소유한 지출이 아닌 경우 예외를 발생시킵니다.
     */
    public void deleteExpense(Long userId, Long expenseId) {
        // 지출 및 사용자 정보 가져오기
        Expense expense = getExpense(expenseId);
        User user = getUser(userId);

        // 소유자 확인 후 삭제
        if (!expense.getUser().equals(user)) {
            throw new CustomException(NOT_MY_EXPENSE);
        }

        // 지출 삭제
        expenseRepository.delete(expense);
    }

}