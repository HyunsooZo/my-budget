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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.mybudget.exception.ErrorCode.*;

@Slf4j
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
    @Transactional
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

    /**
     * 이 메서드는 오늘의 지출 내역을 조회하고, 사용자별로 예상 소비 금액에 대한 알림을 제공합니다.
     * 사용자 목록을 가져온 후 각 사용자에 대해 오늘의 지출 내역을 확인하고,
     * 예상 소비 금액 대비 실제 소비 금액의 비율을 계산하여 알림을 보냅니다.
     * 프로젝트 기간이 매우짧은 프로젝트인 관계로 로그로 알림을 대체하였습니다.
     *
     * @Transactional(readOnly = true)으로 선언되었으며, 읽기 전용 트랜잭션으로 동작합니다.
     */
    @Transactional(readOnly = true)
    public void notifyTodayExpense() {
        // 사용자 목록 조회
        List<User> users = userRepository.findAll();

        // 카테고리별 지출 내역을 저장하는 맵
        Map<Categories, BigDecimal> categoriesBigDecimalMap = new HashMap<>();

        // 오늘 지출 내역 여부를 확인하는 AtomicBoolean
        AtomicBoolean noExpenseToday = new AtomicBoolean(true);

        // 각 사용자에 대해 지출 내역을 확인하고 맵에 저장
        users.forEach(user -> {
            // 오늘의 지출 내역을 가져와 맵에 추가
            expenseRepository.getExpensesByPeriod(
                    user.getId(),
                    Date.valueOf(LocalDate.now()),
                    Date.valueOf(LocalDate.now()),
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(1000000000L)
            ).forEach(expense -> {
                categoriesBigDecimalMap.put(
                        expense.getCategory(),
                        categoriesBigDecimalMap.getOrDefault(
                                expense.getCategory(), BigDecimal.ZERO
                        ).add(expense.getAmount())
                );
            });

            // 각 카테고리별 지출 금액을 계산하고 지출이 있을 경우 정보를 로깅하고 플래그를 설정
            AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
            categoriesBigDecimalMap.keySet().forEach(categories -> {
                if (categoriesBigDecimalMap.get(categories).compareTo(BigDecimal.ZERO) > 0) {
                    log.info(user.getEmail() + "님의 " +
                            categories + " 카테고리 지출 금액은 " +
                            categoriesBigDecimalMap.get(categories) + "원 입니다.");
                    totalAmount.set(totalAmount.get().add(categoriesBigDecimalMap.get(categories)));
                    noExpenseToday.set(false);
                }
            });

            // 오늘 지출 내역이 있을 경우 예상 소비 금액 대비 실제 소비 금액의 비율 계산 후 알림
            if (!noExpenseToday.get()) {
                BigDecimal expectedAmount = budgetRepository.findByUserAndDate(user,
                                Date.valueOf(LocalDate.now()))
                        .stream().map(Budget::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal expectedExpense = expectedAmount.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
                BigDecimal ratio = totalAmount.get().divide(expectedExpense, 2,
                        RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                log.info(user.getEmail() + "님의 오늘 예상 소비 금액은 " + expectedExpense +
                        "원 이었습니다. 금일 실제 소비금액은 " + totalAmount + "원 입니다. " +
                        "예상 소비금액 대비" + ratio + "% 지출했습니다.");
            }
        });
    }
    /**
     * 이 메서드는 사용자에게 추천할 예상 지출을 계산하고, 각 카테고리에 대한 추천 금액 제공
     * @Transactional(readOnly = true)로 선언되어 읽기 전용 트랜잭션으로 동작
     */
    @Transactional(readOnly = true)
    public void recommendExpenses() {
        // 사용자 목록 조회
        List<User> users = userRepository.findAll();

        // 카테고리별 지출 내역을 저장하는 맵
        Map<Categories, BigDecimal> categoriesBigDecimalMap = new HashMap<>();

        // 시작일을 추적하는 변수
        AtomicReference<Date> startDate = new AtomicReference<>(null); // 초기값을 null로 설정

        // 각 사용자에 대해 추천 지출 계산
        users.forEach(user -> {
            // 해당 사용자의 예산 가져오기
            budgetRepository.findByUserAndDate(user, Date.valueOf(LocalDate.now()))
                    .forEach(budget -> {
                        // 카테고리별 지출 내역 계산
                        categoriesBigDecimalMap.put(
                                budget.getCategory(),
                                categoriesBigDecimalMap.getOrDefault(
                                        budget.getCategory(), BigDecimal.ZERO
                                ).add(budget.getAmount())
                        );

                        // 시작일 업데이트
                        Date budgetStartDate = budget.getStartDate();
                        if (startDate.get() == null ||
                                (budgetStartDate != null &&
                                        budgetStartDate.before(startDate.get()))) {
                            startDate.set(budgetStartDate);
                        }
                    });

            // startDate가 null이면 현재 날짜로 설정
            if (startDate.get() == null) {
                startDate.set(Date.valueOf(LocalDate.now()));
            }

            // 이번 달의 마지막 날짜 계산
            LocalDate lastDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            long daysLeftInThisMonth = ChronoUnit.DAYS.between(LocalDate.now(), lastDate) + 1;

            // 사용자의 오늘의 지출 내역 조회하여 카테고리별 지출 내역 갱신
            expenseRepository.getExpensesByPeriod(
                    user.getId(),
                    Date.valueOf(LocalDate.now()),
                    Date.valueOf(LocalDate.now()),
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(1000000000L)
            ).forEach(expense -> {
                categoriesBigDecimalMap.put(
                        expense.getCategory(),
                        categoriesBigDecimalMap.getOrDefault(
                                expense.getCategory(), BigDecimal.ZERO
                        ).subtract(expense.getAmount())
                );
            });

            // 카테고리별 추천 금액 계산
            AtomicReference<BigDecimal> totalRecommendationAmount = new AtomicReference<>(BigDecimal.ZERO);
            categoriesBigDecimalMap.forEach((category, amount) -> {
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    amount = BigDecimal.valueOf(1000);
                }
                BigDecimal dividedAmount = amount.divide(BigDecimal.valueOf(daysLeftInThisMonth), 2,
                        RoundingMode.HALF_UP);
                log.info(user.getEmail() + "님, " + category +
                        " 카테고리 추천 금액은 " + dividedAmount + "원 입니다.");
                totalRecommendationAmount.set(totalRecommendationAmount.get().add(dividedAmount));
            });

            // 사용자의 총 추천 소비 금액 로깅
            log.info(user.getEmail() + "총 추천 소비금액은 " + totalRecommendationAmount + "원입니다.");
        });
    }

}