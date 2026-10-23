package com.ahorra.features.session.repository;

import com.ahorra.features.session.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);
    Optional<UserSession> findByUserIdAndIsActiveTrue(Long userId);
    boolean existsByUserIdAndIsActiveTrue(Long userId);
}
