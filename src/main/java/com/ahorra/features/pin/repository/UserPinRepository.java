package com.ahorra.features.pin.repository;

import com.ahorra.features.pin.model.UserPin;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPinRepository extends JpaRepository<UserPin, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pin from UserPin pin where pin.userId = :userId")
    Optional<UserPin> findForUpdate(@Param("userId") Long userId);
}
