package com.ahorra.features.capture.repository;

import com.ahorra.features.capture.model.RawEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RawEntryRepository extends JpaRepository<RawEntry, Long> {
    Optional<RawEntry> findByUserIdAndClientRequestId(Long userId, String clientRequestId);
}
