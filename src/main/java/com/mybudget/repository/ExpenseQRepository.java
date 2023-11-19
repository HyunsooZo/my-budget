package com.mybudget.repository;

import com.mybudget.domain.Expense;
import com.mybudget.dto.AmountsOfCategoryDto;
import com.mybudget.enums.Categories;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.List;

public interface ExpenseQRepository {
    List<Expense> getExpensesByPeriod(Long userId,
                                      Date startDate,
                                      Date endDate,
                                      BigDecimal minimumAmount,
                                      BigDecimal maximumAmount);

    List<AmountsOfCategoryDto> getAmountsByPeriod(Long userId,
                                                  Date startDate,
                                                  Date endDate,
                                                  BigDecimal minimumAmount,
                                                  BigDecimal maximumAmount);

    BigDecimal getTotalAmountByPeriod(Long userId,
                                      Date startDate,
                                      Date endDate,
                                      BigDecimal minimumAmount,
                                      BigDecimal maximumAmount);

    List<Expense> getExpensesByPeriodWithCategory(Long userId,
                                                  Date startDate,
                                                  Date endDate,
                                                  Categories category,
                                                  BigDecimal minimumAmount,
                                                  BigDecimal maximumAmount);


    List<AmountsOfCategoryDto> getAmountsByPeriodWithCategory(Long userId,
                                                              Date startDate,
                                                              Date endDate,
                                                              Categories category,
                                                              BigDecimal minimumAmount,
                                                              BigDecimal maximumAmount);

    BigDecimal getTotalAmountByPeriodWithCategory(Long userId,
                                                  Date startDate,
                                                  Date endDate,
                                                  Categories category,
                                                  BigDecimal minimumAmount,
                                                  BigDecimal maximumAmount);

    List<Expense> getExpensesByMonth(Long userId,
                                     Date firstDate,
                                     Date lastDate);

    BigDecimal getAmountAverageByDayOfWeek(Long userId,
                                           Date endDate,
                                           DayOfWeek dayOfWeek);

    BigDecimal getAmountOfTodayByDayOfWeek(Long userId,
                                           Date today,
                                           DayOfWeek dayOfWeekOfToday);
}
