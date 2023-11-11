package com.mybudget.service;

import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.dto.BudgetDto;
import com.mybudget.dto.BudgetEditRequestDto;
import com.mybudget.exception.CustomException;
import com.mybudget.exception.ErrorCode;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.mybudget.exception.ErrorCode.USER_INFO_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class BudgetManagementService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

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

    @Transactional
    public void editBudget(Long userId, Long budgetId, BudgetEditRequestDto budgetEditRequestDto) {
        User user = getUser(userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new CustomException(ErrorCode.BUDGET_NOT_FOUND));

        if (!budget.getUser().equals(user)) {
            throw new CustomException(ErrorCode.NOT_MY_BUDGET);
        }

        budget.setAmount(budgetEditRequestDto.getAmount());
    }
}
