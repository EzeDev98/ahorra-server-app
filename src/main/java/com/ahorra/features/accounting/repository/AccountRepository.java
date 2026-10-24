package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIdAndUserId(Long id, Long userId);
    List<Account> findAllByUserIdAndIdIn(Long userId, Collection<Long> ids);
    List<Account> findAllByUserIdAndActiveTrue(Long userId);
    Optional<Account> findByUserIdAndName(Long userId, String name);
}
