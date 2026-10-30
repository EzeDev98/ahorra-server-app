package com.ahorra.features.user.repository;

import com.ahorra.features.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    @Query("select user.id from User user where user.reminderEnabled = true")
    List<Long> findReminderEnabledUserIds();
}
