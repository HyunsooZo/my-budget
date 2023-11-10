package com.mybudget.repository;

import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.enums.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);

    Optional<Budget> findByUserAndCategory(User user, Categories category);
}
