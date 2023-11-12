package com.mybudget.service;

import com.mybudget.domain.Expense;
import com.mybudget.domain.User;
import com.mybudget.dto.ExpenseCreationRequestDto;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.ExpenseRepository;
import com.mybudget.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.mybudget.exception.ErrorCode.USER_INFO_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 지출 내역 생성
     *
     * @param userId 사용자 식별자
     * @param expenseCreationRequestDto 지출 생성 요청 DTO
     *
     * @throws CustomException 사용자 정보를 찾을 수 없는 경우 예외를 발생시킵니다.
     */
    @Transactional
    public void createExpense(Long userId, ExpenseCreationRequestDto expenseCreationRequestDto) {

        User user = getUser(userId);

        expenseRepository.save(Expense.from(user, expenseCreationRequestDto));
    }

    /**
     * 주어진 사용자 식별자에 해당하는 사용자 조회
     *
     * @param userId 사용자 식별자
     * @return 주어진 식별자에 해당하는 사용자
     *
     * @throws CustomException 사용자 정보를 찾을 수 없는 경우 예외를 발생시킵니다.
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_INFO_NOT_FOUND));
    }

}
