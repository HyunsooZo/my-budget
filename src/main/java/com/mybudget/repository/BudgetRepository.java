package com.mybudget.repository;

import com.mybudget.domain.Budget;
import com.mybudget.domain.User;
import com.mybudget.enums.Categories;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);

    List<Budget> findByUserAndCategory(User user, Categories category);

    @Query("SELECT SUM(e.amount) FROM Budget e")
    BigDecimal getTotalAmount();

    @Query("SELECT SUM(e.amount) FROM Budget e WHERE e.category = :category")
    BigDecimal getAmountOfCategory(@Param("category") Categories category);

    Optional<Budget> findByCategory(Categories category);

    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.startDate <= :date AND b.endDate >= :date")
    List<Budget> findByUserAndDate(@Param("user") User user, @Param("date") Date date);
}
