package com.ahorra.features.passwordreset.repository;

import com.ahorra.features.passwordreset.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    List<PasswordReset> findAllByUserIdAndUsedAtIsNull(Long userId);
    Optional<PasswordReset> findByResetTokenHash(String resetTokenHash);
}
