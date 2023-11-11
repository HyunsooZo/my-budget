package com.mybudget.service;

import com.mybudget.domain.Budget;
import com.mybudget.domain.CategoryRatio;
import com.mybudget.enums.Categories;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.CategoryRatioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BudgetRecommendationService {
    private final BudgetRepository budgetRepository;
    private final CategoryRatioRepository categoryRatioRepository;
    private final List<Categories> CATEGORIES = Arrays.asList(Categories.values());

    /**
     * 예산 비율을 설정.  각 카테고리의 예산 비율을 계산하여 CategoryRatio 엔티티에 저장
     */
    @Transactional
    public void setRatiosPerBudgets() {

        // 전체 예산의 총액을 가져옵니다.
        BigDecimal totalAmount = budgetRepository.getTotalAmount();

        // 각 카테고리에 대해 예산 총액과 전체 예산에 대한 비율을 계산
        CATEGORIES.forEach(category -> {
            BigDecimal amountOfCategory = BigDecimal.ZERO;
            BigDecimal ratio = BigDecimal.ZERO;

            Optional<Budget> budgetOptional = budgetRepository.findByCategory(category);

            if (budgetOptional.isPresent()) {
                amountOfCategory = budgetRepository.getAmountOfCategory(category);
                ratio = amountOfCategory.divide(totalAmount, 2, RoundingMode.HALF_UP);
            }

            // 해당 카테고리의 비율을 CategoryRatio 엔티티에 저장하거나 업데이트
            Optional<CategoryRatio> targetCategory = categoryRatioRepository.findByCategory(category);

            if (targetCategory.isPresent()) {
                // 이미 존재하는 경우 비율을 업데이트
                targetCategory.get().setRatio(ratio.doubleValue());
            } else {
                // 존재하지 않는 경우 새로운 CategoryRatio 엔티티 생성 및 저장
                categoryRatioRepository.save(
                        CategoryRatio.builder()
                                .category(category)
                                .ratio(ratio.doubleValue())
                                .build());
            }
        });
    }
}
