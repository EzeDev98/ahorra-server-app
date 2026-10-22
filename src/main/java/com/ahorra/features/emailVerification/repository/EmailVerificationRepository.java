package com.ahorra.features.emailVerification.repository;

import com.ahorra.features.emailVerification.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    List<EmailVerification> findAllByUserIdAndUsedAtIsNull(Long userId);
}
