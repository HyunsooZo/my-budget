package com.mybudget.component;

import com.mybudget.service.BudgetRecommendationService;
import com.mybudget.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CronScheduler {

    private final BudgetRecommendationService budgetRecommendationService;
    private final ExpenseService expenseService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00시에 실행
    @Transactional
    public void calculateAndSaveCategoryRatio() {
        budgetRecommendationService.setRatiosPerBudgets();
    }

    @Scheduled(cron = "0 0 20 * * *") // 매일 20시에 실행
    @Transactional(readOnly = true)
    public void notifyTodayExpense() {
        expenseService.notifyTodayExpense();
    }

    @Scheduled(cron = "0 0 8 * * *") // 매일 08시에 실행
    @Transactional(readOnly = true)
    public void recommendDailyExpenses() {
        expenseService.recommendExpenses();
    }

}
