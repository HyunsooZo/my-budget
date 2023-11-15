package com.mybudget.service;

import com.mybudget.domain.Expense;
import com.mybudget.dto.CategoryExpenseRatioDto;
import com.mybudget.enums.Categories;
import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StatisticService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 통계 데이터를 가져오는 메서드
     *
     * @param userId 사용자 식별자
     * @return 통계 응답 DTO
     */
    @Transactional
    public List<CategoryExpenseRatioDto> getCategoryStatistics(Long userId, Date today) {
        Date thisMonthStartDate = Date.valueOf(today.toLocalDate().minusMonths(1));
        Date lastMonthStartDate = Date.valueOf(today.toLocalDate().minusMonths(2));
        Date lastMonthEndDate = Date.valueOf(today.toLocalDate().minusMonths(1).minusDays(1));

        List<Expense> lastMonthExpenses = expenseRepository.getExpensesByMonth(
                userId, lastMonthStartDate, lastMonthEndDate
        );

        List<Expense> thisMonthExpenses = expenseRepository.getExpensesByMonth(
                userId, thisMonthStartDate, today
        );

        Map<Categories, BigDecimal> thisMonthExpenseStatistic =
                getExpenseStatistic(thisMonthExpenses);
        Map<Categories, BigDecimal> lastMonthExpenseStatistic =
                getExpenseStatistic(lastMonthExpenses);

        return getExpenseRatio(thisMonthExpenseStatistic, lastMonthExpenseStatistic);

    }

    /**
     * 현재 달과 전 달의 지출을 비교하여 비율을 계산하는 메서드
     *
     * @param thisMonthExpense 현재 달의 카테고리별 지출 맵
     * @param lastMonthExpense 전 달의 카테고리별 지출 맵
     * @return 카테고리별 지출 비율을 나타내는 CategoryExpenseRatioDto 리스트
     */
    private List<CategoryExpenseRatioDto> getExpenseRatio(
            Map<Categories, BigDecimal> thisMonthExpense,
            Map<Categories, BigDecimal> lastMonthExpense) {

        List<CategoryExpenseRatioDto> categoryExpenses = new ArrayList<>();

        thisMonthExpense.keySet().forEach(category -> {
            BigDecimal thisMonthAmount =
                    thisMonthExpense.getOrDefault(category, BigDecimal.ONE);
            BigDecimal lastMonthAmount =
                    lastMonthExpense.getOrDefault(category, BigDecimal.ONE);
            BigDecimal ratio =
                    thisMonthAmount.divide(lastMonthAmount, 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

            categoryExpenses.add(
                    CategoryExpenseRatioDto.builder()
                            .categories(category)
                            .ratio(ratio.doubleValue())
                            .build()
            );
        });
        return categoryExpenses;
    }

    /**
     * 이번 달 지출 목록을 받아 카테고리별 지출을 계산하여 맵으로 반환
     *
     * @param thisMonthExpenses 이번 달의 지출 목록
     * @return 카테고리별로 누적된 지출을 담은 맵
     */
    private Map<Categories, BigDecimal> getExpenseStatistic(List<Expense> thisMonthExpenses) {
        return thisMonthExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory, // Expense 객체의 카테고리를 기준으로 그룹화
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                        // 각 카테고리별로 지출 합계를 계산하여 맵에 추가
                ));
    }
}
