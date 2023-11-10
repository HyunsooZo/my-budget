package com.mybudget.repository;

import com.mybudget.domain.CategoryRatio;
import com.mybudget.enums.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRatioRepository extends JpaRepository<CategoryRatio, Categories> {

    Optional<CategoryRatio> findByCategory(Categories category);
}
