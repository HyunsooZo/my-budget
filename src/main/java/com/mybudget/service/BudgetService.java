package com.mybudget.service;

import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetEditRequestDto;
import com.mybudget.dto.BudgetSettingRequestDto;
import com.mybudget.enums.Categories;
import com.mybudget.exception.CustomException;
import com.mybudget.exception.ErrorCode;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.mybudget.exception.ErrorCode.USER_INFO_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class BudgetService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    /**
     * 모든 카테고리 반환
     */
    public List<Categories> getCategories() {
        return Arrays.asList(Categories.values());
    }

    /**
     * 사용자의 예산을 설정
     *
     * @param userId                  사용자 ID
     * @param budgetSettingRequestDto 예산 설정 요청 DTO
     * @throws CustomException 유저 정보가 없을 때 발생하는 예외
     */
    @Transactional
    public void createBudget(Long userId,
                             BudgetSettingRequestDto budgetSettingRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_INFO_NOT_FOUND));

        List<BudgetDto> budgetDtos = budgetSettingRequestDto.getBudgets();

        budgetDtos.forEach(budgetDto -> {
            LocalDate newBudgetStart = dateToLocalDate(budgetDto.getStartDate());
            LocalDate newBudgetEnd = dateToLocalDate(budgetDto.getEndDate());

            if (newBudgetStart.isAfter(newBudgetEnd)) {
                throw new CustomException(ErrorCode.INVALID_BUDGET_DATE);
            }

            List<Budget> existingBudgets = budgetRepository.findByUserAndCategory(
                    user, budgetDto.getCategory()
            );

            existingBudgets.forEach(existingBudget -> {
                LocalDate existingBudgetStart =
                        dateToLocalDate(existingBudget.getStartDate());
                LocalDate existingBudgetEnd =
                        dateToLocalDate(existingBudget.getEndDate());

                if ((newBudgetStart.isBefore(existingBudgetEnd) ||
                        newBudgetStart.isEqual(existingBudgetEnd)) &&
                        (newBudgetEnd.isAfter(existingBudgetStart) ||
                                newBudgetEnd.isEqual(existingBudgetStart))) {
                    throw new CustomException(ErrorCode.BUDGET_ALREADY_EXISTS);
                }
            });

            budgetRepository.save(Budget.from(user, budgetDto));
        });
    }

    /**
     * Date를 LocalDate로 변환하는 메서드
     *
     * @param date 변환할 Date
     * @return 변환된 LocalDate
     */
    private LocalDate dateToLocalDate(Date date) {
        return date.toLocalDate();
    }

    /**
     * 사용자의 예산 설정 조회
     *
     * @param userId 사용자 ID
     * @return 사용자의 예산 목록
     * @throws CustomException 사용자 정보를 찾을 수 없을 때 발생하는 예외
     */
    public List<BudgetDto> getMyBudgets(Long userId) {
        User user = getUser(userId);

        List<Budget> budgets = budgetRepository.findByUser(user);

        return budgets.stream()
                .map(BudgetDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 정보 조회
     *
     * @param userId 사용자 ID
     * @return 해당 ID에 해당하는 사용자 정보
     * @throws CustomException 사용자 정보를 찾을 수 없을 때 발생하는 예외
     */
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_INFO_NOT_FOUND));
    }

    /**
     * 예산 수정
     *
     * @param userId               사용자 ID
     * @param budgetId             수정할 예산 ID
     * @param budgetEditRequestDto 수정할 예산 정보를 담은 DTO
     * @throws CustomException 예산이 찾아지지 않을 경우 예외 발생
     */
    @Transactional
    public void editBudget(Long userId, Long budgetId, BudgetEditRequestDto budgetEditRequestDto) {
        User user = getUser(userId);

        Budget budget = getBudget(budgetId);

        if (!budget.getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_MY_BUDGET);
        }

        budget.setAmount(budgetEditRequestDto.getAmount());
    }

    /**
     * 예산 삭제
     *
     * @param userId   사용자 ID
     * @param budgetId 삭제할 예산 ID
     * @throws CustomException 예산이 찾아지지 않을 경우 예외 발생
     */
    public void deleteBudget(Long userId, Long budgetId) {
        User user = getUser(userId);

        Budget budget = getBudget(budgetId);

        if (!budget.getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_MY_BUDGET);
        }

        budgetRepository.delete(budget);
    }

    /**
     * 특정 예산을 조회
     *
     * @param budgetId 가져올 예산 ID
     * @return 주어진 ID에 해당하는 예산
     * @throws CustomException 예산이 찾아지지 않을 경우 예외 발생
     */
    private Budget getBudget(Long budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new CustomException(ErrorCode.BUDGET_NOT_FOUND));
    }
}
