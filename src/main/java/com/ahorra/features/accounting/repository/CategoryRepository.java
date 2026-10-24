package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    Optional<Category> findByUserIdAndName(Long userId, String name);
    List<Category> findAllByUserIdAndActiveTrue(Long userId);
}
