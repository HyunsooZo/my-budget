package com.mybudget.repository;

import com.mybudget.domain.Expense;
import com.mybudget.domain.QExpense;
import com.mybudget.dto.AmountsOfCategoryDto;
import com.mybudget.enums.Categories;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.querydsl.core.types.dsl.Expressions.constant;

@RequiredArgsConstructor
@Repository
public class ExpenseQRepositoryImpl implements ExpenseQRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final List<Categories> categories = Arrays.asList(Categories.values());

    /**
     * 지정된 기간 내의 지출 데이터를 가져오는 메서드입니다.
     *
     * @param userId        사용자 ID
     * @param startDate     시작 날짜
     * @param endDate       종료 날짜
     * @param minimumAmount 최소 금액
     * @param maximumAmount 최대 금액
     * @return 지출 데이터 목록
     */
    @Override
    public List<Expense> getExpensesByPeriod(Long userId,
                                             Date startDate,
                                             Date endDate,
                                             BigDecimal minimumAmount,
                                             BigDecimal maximumAmount) {
        QExpense expense = QExpense.expense;

        return jpaQueryFactory.selectFrom(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.between(startDate, endDate))
                        .and(expense.amount.between(minimumAmount, maximumAmount)))
                .orderBy(expense.expenseDate.desc())
                .fetch();
    }

    /**
     * 지정된 기간 내의 지출 데이터를 가져오는 메서드
     *
     * @param userId        사용자 ID
     * @param startDate     시작 날짜
     * @param endDate       종료 날짜
     * @param category      카테고리
     * @param minimumAmount 최소 금액
     * @param maximumAmount 최대 금액
     * @return 지출 데이터 목록
     */
    @Override
    public List<Expense> getExpensesByPeriodWithCategory(Long userId,
                                                         Date startDate,
                                                         Date endDate,
                                                         Categories category,
                                                         BigDecimal minimumAmount,
                                                         BigDecimal maximumAmount) {
        QExpense expense = QExpense.expense;

        return jpaQueryFactory.selectFrom(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.between(startDate, endDate))
                        .and(expense.amount.between(minimumAmount, maximumAmount))
                        .and(expense.category.eq(category)))
                .orderBy(expense.expenseDate.desc())
                .fetch();
    }

    /**
     * 카테고리별 지출 데이터를 가져오는 메서드
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 카테고리별 지출 데이터 목록
     */
    @Override
    public List<AmountsOfCategoryDto> getAmountsByPeriod(Long userId,
                                                         Date startDate,
                                                         Date endDate,
                                                         BigDecimal minimumAmount,
                                                         BigDecimal maximumAmount) {
        QExpense expense = QExpense.expense;

        BooleanExpression predicate = createExpensForAll(
                expense, userId, startDate, endDate, minimumAmount, maximumAmount);

        return getAmountsOfCategoryDtos(expense, predicate);
    }

    /**
     * 카테고리별 지출 데이터를 가져오는 메서드
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 카테고리별 지출 데이터 목록
     */
    @Override
    public List<AmountsOfCategoryDto> getAmountsByPeriodWithCategory(Long userId,
                                                                     Date startDate,
                                                                     Date endDate,
                                                                     Categories category,
                                                                     BigDecimal minimumAmount,
                                                                     BigDecimal maximumAmount) {
        QExpense expense = QExpense.expense;

        BooleanExpression predicate = createExpenseForACategory(
                expense, userId, startDate, endDate, category, minimumAmount, maximumAmount);

        return getAmountsOfCategoryDtos(expense, predicate);
    }

    /**
     * 카테고리별 지출 데이터를 가져오는 메서드입니다.
     *
     * @param expense   QueryDSL의 QExpense
     * @param predicate 지출 데이터를 필터링하는 데 사용되는 조건식
     * @return 카테고리별 지출 데이터의 목록
     */
    private List<AmountsOfCategoryDto> getAmountsOfCategoryDtos(QExpense expense, BooleanExpression predicate) {
        return categories.stream()
                .map(eachCategory -> {
                    BooleanExpression categoryPredicate = predicate.and(expense.category.eq(eachCategory));

                    BigDecimal totalAmount = jpaQueryFactory
                            .select(expense.amount.sum())
                            .from(expense)
                            .where(categoryPredicate)
                            .fetchOne();

                    return totalAmount != null ?
                            AmountsOfCategoryDto.builder()
                                    .category(eachCategory)
                                    .totalAmount(totalAmount)
                                    .build() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 지정된 기간 내의 지출 총액을 계산하는 메서드입니다.
     *
     * @param userId        사용자 ID
     * @param startDate     시작 날짜
     * @param endDate       종료 날짜
     * @param minimumAmount 최소 금액
     * @param maximumAmount 최대 금액
     * @return 지정된 기간 내의 지출 총액
     */
    @Override
    public BigDecimal getTotalAmountByPeriod(Long userId,
                                             Date startDate,
                                             Date endDate,
                                             BigDecimal minimumAmount,
                                             BigDecimal maximumAmount) {

        QExpense expense = QExpense.expense;

        BooleanExpression predicate = createExpensForAll(
                expense, userId, startDate, endDate, minimumAmount, maximumAmount);

        return jpaQueryFactory.select(expense.amount.sum())
                .from(expense)
                .where(predicate)
                .fetchOne();
    }

    /**
     * 지정된 기간 및 카테고리 내의 지출 총액을 계산하는 메서드입니다.
     *
     * @param userId        사용자 ID
     * @param startDate     시작 날짜
     * @param endDate       종료 날짜
     * @param category      카테고리
     * @param minimumAmount 최소 금액
     * @param maximumAmount 최대 금액
     * @return 지정된 기간 및 카테고리 내의 지출 총액
     */
    @Override
    public BigDecimal getTotalAmountByPeriodWithCategory(Long userId,
                                                         Date startDate,
                                                         Date endDate,
                                                         Categories category,
                                                         BigDecimal minimumAmount,
                                                         BigDecimal maximumAmount) {

        QExpense expense = QExpense.expense;

        BooleanExpression predicate = createExpenseForACategory(
                expense, userId, startDate, endDate, category, minimumAmount, maximumAmount);

        return jpaQueryFactory.select(expense.amount.sum())
                .from(expense)
                .where(predicate)
                .fetchOne();
    }

    /**
     * 지정된 기간 내의 모든 지출 데이터를 필터링하는 조건식을 생성합니다.
     *
     * @param expense       QueryDSL의 QExpense
     * @param userId        사용자 ID
     * @param startDate     시작 날짜
     * @param endDate       종료 날짜
     * @param minimumAmount 최소 금액
     * @param maximumAmount 최대 금액
     * @return 지정된 기간 내의 지출 데이터를 필터링하는 조건식
     */
    private BooleanExpression createExpensForAll(QExpense expense,
                                                 Long userId,
                                                 Date startDate,
                                                 Date endDate,
                                                 BigDecimal minimumAmount,
                                                 BigDecimal maximumAmount) {
        return expense.user.id.eq(userId)
                .and(expense.expenseDate.between(startDate, endDate))
                .and(expense.amount.between(constant(minimumAmount), constant(maximumAmount)))
                .and(expense.excluding.eq(false));
    }

    /**
     * 지정된 기간 및 카테고리 내의 지출 데이터를 필터링하는 조건식을 생성합니다.
     *
     * @param expense       QueryDSL의 QExpense
     * @param userId        사용자 ID
     * @param startDate     시작 날짜
     * @param endDate       종료 날짜
     * @param category      카테고리
     * @param minimumAmount 최소 금액
     * @param maximumAmount 최대 금액
     * @return 지정된 기간 및 카테고리 내의 지출 데이터를 필터링하는 조건식
     */
    private BooleanExpression createExpenseForACategory(QExpense expense,
                                                        Long userId,
                                                        Date startDate,
                                                        Date endDate,
                                                        Categories category,
                                                        BigDecimal minimumAmount,
                                                        BigDecimal maximumAmount) {
        return expense.user.id.eq(userId)
                .and(expense.expenseDate.between(startDate, endDate))
                .and(expense.amount.between(constant(minimumAmount), constant(maximumAmount)))
                .and(expense.category.eq(category))
                .and(expense.excluding.eq(false));
    }

    /**
     * 특정 월의 지출 목록을 가져옵니다.
     *
     * @param userId    지출 목록을 가져올 사용자의 ID입니다.
     * @param firstDate 해당 월의 시작 날짜입니다.
     * @param lastDate  해당 월의 끝 날짜입니다.
     * @return 사용자의 특정 월에 대한 지출 목록입니다.
     */
    @Override
    public List<Expense> getExpensesByMonth(Long userId, Date firstDate, Date lastDate) {
        QExpense expense = QExpense.expense;

        return jpaQueryFactory.selectFrom(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.between(firstDate, lastDate)))
                .fetch();
    }

    /**
     * 종료일 이전까지의 해당 요일에 대한 지출 평균을 계산합니다.
     *
     * @param userId    사용자 ID
     * @param endDate   종료일
     * @param dayOfWeek 해당 요일
     * @return 종료일 이전까지의 해당 요일에 대한 지출 평균
     */
    @Override
    public BigDecimal getAmountAverageByDayOfWeek(Long userId, Date endDate, DayOfWeek dayOfWeek) {
        QExpense expense = QExpense.expense;

        // 종료일 이전까지의 해당 요일에 대한 총 지출액
        BigDecimal totalAmount = jpaQueryFactory.select(expense.amount.sum())
                .from(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.before(endDate)) // 종료일 이전
                        .and(expense.dayOfWeek.eq(dayOfWeek)))   // 해당 요일
                .fetchOne();

        // 종료일까지의 해당 요일에 대한 지출 건수
        Long count = jpaQueryFactory.select(expense.count())
                .from(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.before(endDate)) // 종료일 이전
                        .and(expense.dayOfWeek.eq(dayOfWeek)))   // 해당 요일
                .fetchOne();

        // 평균 계산: 총액 / 건수
        if (count != null && count != 0) {
            return totalAmount != null ?
                    totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ONE; // totalAmount이 null인 경우 기본값 1 반환
        } else {
            return BigDecimal.ONE; // count가 null이거나 0인 경우 기본값 1 반환
        }
    }

    /**
     * 오늘의 특정 요일에 대한 지출을 계산합니다.
     *
     * @param userId           사용자 ID
     * @param today            오늘의 날짜
     * @param dayOfWeekOfToday 오늘의 요일
     * @return 오늘의 특정 요일에 대한 지출
     */
    @Override
    public BigDecimal getAmountOfTodayByDayOfWeek(Long userId,
                                                  Date today,
                                                  DayOfWeek dayOfWeekOfToday) {
        QExpense expense = QExpense.expense;

        BigDecimal sum = jpaQueryFactory.select(expense.amount.sum())
                .from(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.eq(today))        // 오늘의 날짜
                        .and(expense.dayOfWeek.eq(dayOfWeekOfToday))) // 오늘의 요일
                .fetchOne();

        Long count = jpaQueryFactory.select(expense.count())
                .from(expense)
                .where(expense.user.id.eq(userId)
                        .and(expense.expenseDate.eq(today))        // 오늘의 날짜
                        .and(expense.dayOfWeek.eq(dayOfWeekOfToday))) // 오늘의 요일
                .fetchOne();

        // 평균 계산: 총액 / 건수
        if (count != null && count != 0) {
            return sum != null ?
                    sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ONE; // sum이 null인 경우 기본값 1 반환
        } else {
            return BigDecimal.ONE; // count가 null이거나 0인 경우 기본값 1 반환
        }
    }
}