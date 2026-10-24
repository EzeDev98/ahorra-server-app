package com.ahorra.features.accounting.service.impl;

import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultCategoryService {

    public static final List<String> DEFAULTS = List.of(
            "Food",
            "Transport",
            "Airtime and data",
            "Bills and utilities",
            "Rent",
            "Health",
            "Education",
            "Shopping",
            "Entertainment",
            "Family and gifts",
            "Other"
    );

    private final CategoryRepository categoryRepository;

    public void ensureDefaults(Long userId) {
        if (!categoryRepository.findAllByUserIdAndActiveTrue(userId).isEmpty()) {
            return;
        }
        for (String name : DEFAULTS) {
            if (categoryRepository.findByUserIdAndName(userId, name).isPresent()) {
                continue;
            }
            Category category = new Category();
            category.setUserId(userId);
            category.setName(name);
            category.setActive(true);
            category.setSystem(true);
            try {
                categoryRepository.save(category);
            } catch (DataIntegrityViolationException e) {
                log.debug("Default category '{}' already created for user {}", name, userId);
            }
        }
    }
}
