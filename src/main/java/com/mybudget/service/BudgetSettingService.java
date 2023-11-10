package com.mybudget.service;

import com.mybudget.domain.Budget;
import com.mybudget.domain.CategoryRatio;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetSettingRequestDto;
import com.mybudget.enums.Categories;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.CategoryRatioRepository;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mybudget.exception.ErrorCode.USER_INFO_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class BudgetSettingService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRatioRepository categoryRatioRepository;

    /**
     * 모든 카테고리 반환
     */
    public List<Categories> getCategories() {
        return Arrays.asList(Categories.values());
    }

    /**
     * 사용자의 예산을 설정하거나 업데이트
     *
     * @param userId                  사용자 ID
     * @param budgetSettingRequestDto 예산 설정 요청 DTO
     * @throws CustomException 유저 정보가 없을 때 발생하는 예외
     */
    @Transactional
    public void createBudget(Long userId, BudgetSettingRequestDto budgetSettingRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_INFO_NOT_FOUND));

        List<BudgetDto> budgetDtos = budgetSettingRequestDto.getBudgets();
        BigDecimal totalAmount = budgetSettingRequestDto.getTotalAmount();

        /*
         만약 예산계획이 없이 총 금액만 입력했다면, 예산 계획 추천하여 저장
         예산계획 추천은 여태 다른 사용자들이 예산을 설정한 퍼센테이지에 따라 추천해 저장.
         */
        if (budgetDtos.isEmpty()) {
            recommendBudgetPlan(user, totalAmount);
        } else {
            processBudgetUpdates(user, budgetDtos, totalAmount);
        }
    }

    /**
     * 예산 업데이트
     *
     * @param user        사용자 정보
     * @param budgetDtos  예산 DTO 리스트
     * @param totalAmount 총 금액
     */
    private void processBudgetUpdates(User user,
                                      List<BudgetDto> budgetDtos,
                                      BigDecimal totalAmount) {

        List<Budget> existingBudgets = budgetRepository.findByUser(user);
        Map<Categories, Budget> existingBudgetsMap = mapExistingBudgets(existingBudgets);

        updateOrCreateBudgets(budgetDtos, totalAmount, user, existingBudgetsMap);
    }

    /**
     * 기존 예산들을 카테고리를 기준으로 매핑하여 Map으로 반환
     *
     * @param existingBudgets 기존 예산 리스트
     * @return 카테고리를 기준으로 한 예산 Map
     */
    private Map<Categories, Budget> mapExistingBudgets(List<Budget> existingBudgets) {
        return existingBudgets.stream()
                .collect(Collectors.toMap(Budget::getCategory, budget -> budget));
    }

    /**
     * 예산을 업데이트하거나 새로 생성
     *
     * @param budgetDtos         예산 DTO 리스트
     * @param totalAmount        총 금액
     * @param user               사용자 정보
     * @param existingBudgetsMap 기존 예산 Map
     */
    private void updateOrCreateBudgets(List<BudgetDto> budgetDtos,
                                       BigDecimal totalAmount,
                                       User user,
                                       Map<Categories, Budget> existingBudgetsMap) {

        budgetDtos.forEach(budgetDto -> {
            Budget existingBudget = existingBudgetsMap.get(budgetDto.getCategory());

            if (existingBudget != null) {
                updateExistingBudget(existingBudget, budgetDto);
            } else {
                budgetRepository.save(Budget.from(user, budgetDto));
            }
        });

        updateCategoryRatios(budgetDtos, totalAmount);
    }

    /**
     * 기존 예산을 업데이트
     *
     * @param existingBudget 기존 예산
     * @param budgetDto      예산 DTO
     */
    private void updateExistingBudget(Budget existingBudget, BudgetDto budgetDto) {
        existingBudget.setAmount(budgetDto.getAmount());
        budgetRepository.save(existingBudget);
    }

    /**
     * 카테고리 비율을 업데이트하거나 새로운 카테고리 비율 삽입
     *
     * @param budgetDtos  예산 DTO 리스트
     * @param totalAmount 총 금액
     */
    private void updateCategoryRatios(List<BudgetDto> budgetDtos, BigDecimal totalAmount) {
        budgetDtos.forEach(budget -> {
            BigDecimal percentage = calculatePercentage(budget.getAmount(), totalAmount);
            updateOrInsertCategoryRatio(budget, percentage);
        });
    }

    /**
     * 금액과 총 금액을 기준으로 백분율 계산
     *
     * @param amount      금액
     * @param totalAmount 총 금액
     * @return 백분율 계산 결과
     */
    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal totalAmount) {
        return amount.divide(totalAmount, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 카테고리 비율을 업데이트하거나 새로운 카테고리 비율 삽입
     *
     * @param budget     예산 DTO
     * @param percentage 백분율
     */
    private void updateOrInsertCategoryRatio(BudgetDto budget, BigDecimal percentage) {
        Optional<CategoryRatio> categoryRatio =
                categoryRatioRepository.findByCategory(budget.getCategory());

        if (categoryRatio.isPresent()) {
            updateCategoryRatio(categoryRatio.get(), percentage);
        } else {
            insertCategoryRatio(budget.getCategory(), percentage);
        }
    }

    /**
     * 기존 카테고리 비율 업데이트
     *
     * @param existingCategoryRatio 기존 카테고리 비율
     * @param percentage            백분율
     */
    private void updateCategoryRatio(CategoryRatio existingCategoryRatio,
                                     BigDecimal percentage) {

        Double averagePercentage = existingCategoryRatio.getRatio();
        Integer count = existingCategoryRatio.getCount();

        Double updatedPercentage =
                (averagePercentage * count + percentage.doubleValue()) / (count + 1);
        existingCategoryRatio.setRatio(updatedPercentage);
        existingCategoryRatio.setCount(count + 1);

        categoryRatioRepository.save(existingCategoryRatio);
    }

    /**
     * 카테고리 비율을 새로 삽입
     *
     * @param category   카테고리
     * @param percentage 백분율
     */
    private void insertCategoryRatio(Categories category, BigDecimal percentage) {
        CategoryRatio newCategoryRatio = CategoryRatio.builder()
                .category(category)
                .ratio(percentage.doubleValue())
                .count(1)
                .build();

        categoryRatioRepository.save(newCategoryRatio);
    }

    /**
     * 추천된 예산 계획 생성
     *
     * @param user        사용자 정보
     * @param totalAmount 총 예산 금액
     */
    private void recommendBudgetPlan(User user, BigDecimal totalAmount) {
        // 모든 카테고리를 불러옴.
        List<Categories> categories = getCategories();

        // 남은 금액을 추적
        BigDecimal remainingAmount = totalAmount;

        // 각 카테고리별로 예산을 설정
        for (Categories category : categories) {

            // 카테고리별 비율을 확인
            Optional<CategoryRatio> categoryRatio =
                    categoryRatioRepository.findByCategory(category);

            if (categoryRatio.isPresent()) {
                // 카테고리별로 할당될 금액 계산
                Double ratio = categoryRatio.get().getRatio();
                BigDecimal allocatedAmount = totalAmount.multiply(BigDecimal.valueOf(ratio / 100)).setScale(2, RoundingMode.HALF_UP);
                remainingAmount = remainingAmount.subtract(allocatedAmount);

                // BudgetDto를 생성하여 각 카테고리별로 예산 설정
                BudgetDto budgetDto = new BudgetDto();
                budgetDto.setCategory(category);
                budgetDto.setAmount(allocatedAmount);

                processBudget(user, budgetDto);
            }
        }

        // 남은 금액을 카테고리 간에 고르게 분배]
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            List<BudgetDto> existingBudgets =
                    budgetRepository.findByUser(user).stream()
                            .map(BudgetDto::from)
                            .collect(Collectors.toList());

            // 존재하는 카테고리의 수 확인
            int numberOfCategories = existingBudgets.size();
            // 남은 금액을 카테고리 수로 나눠서 고르게 분배
            if (numberOfCategories > 0) {
                BigDecimal equalShare =
                        remainingAmount.divide(
                                BigDecimal.valueOf(numberOfCategories),
                                2,
                                RoundingMode.HALF_UP
                        );

                existingBudgets.forEach(budget -> {
                    // 각 카테고리에 고르게 분배된 금액을 추가하여 예산 설정
                    budget.setAmount(budget.getAmount().add(equalShare));
                    processBudget(user, budget);
                });
            }
        }
    }

    /**
     * 사용자의 예산 처리
     *
     * @param user      사용자 정보
     * @param budgetDto 예산 DTO
     */
    private void processBudget(User user, BudgetDto budgetDto) {
        // 사용자와 카테고리에 따른 예산 가져오거나 새로 생성
        Budget existingBudget =
                budgetRepository.findByUserAndCategory(user, budgetDto.getCategory())
                        .orElse(Budget.from(user, budgetDto));

        // 해당 예산에 새로운 금액 설정
        existingBudget.setAmount(budgetDto.getAmount());
        budgetRepository.save(existingBudget);
    }
}
