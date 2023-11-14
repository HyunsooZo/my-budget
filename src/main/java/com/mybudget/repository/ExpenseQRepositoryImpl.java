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
import java.sql.Date;
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
}
