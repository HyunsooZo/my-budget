package com.mybudget.repository;

import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.enums.Categories;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);

    Optional<Budget> findByUserAndCategory(User user, Categories category);

    @Query("SELECT SUM(e.amount) FROM Budget e")
    BigDecimal getTotalAmount();

    @Query("SELECT SUM(e.amount) FROM Budget e WHERE e.category = :category")
    BigDecimal getAmountOfCategory(@Param("category") Categories category);

    Optional<Budget> findByCategory(Categories category);
}
