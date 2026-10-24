package com.ahorra.features.pin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_pins")
public class UserPin {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "pin_hash", nullable = false, length = 100)
    private String pinHash;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "login_verified_at")
    private LocalDateTime loginVerifiedAt;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
