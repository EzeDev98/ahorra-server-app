package com.ahorra.features.capture.repository;

import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.model.PendingEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingEntryRepository extends JpaRepository<PendingEntry, Long> {

    @EntityGraph(attributePaths = "items")
    List<PendingEntry> findAllByRawEntryIdOrderByIdAsc(Long rawEntryId);

    @EntityGraph(attributePaths = "items")
    List<PendingEntry> findAllByUserIdAndStatusOrderByEntryDateAscIdAsc(Long userId, PendingStatus status);
}
