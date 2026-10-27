package com.ahorra.features.capture.repository;

import com.ahorra.features.capture.enums.FlowDirection;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.model.PendingEntry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PendingEntryRepository extends JpaRepository<PendingEntry, Long> {

    @EntityGraph(attributePaths = "items")
    List<PendingEntry> findAllByRawEntryIdOrderByIdAsc(Long rawEntryId);

    @EntityGraph(attributePaths = "items")
    List<PendingEntry> findAllByUserIdAndStatusOrderByEntryDateAscIdAsc(Long userId, PendingStatus status);

    @EntityGraph(attributePaths = "items")
    Optional<PendingEntry> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pendingEntry from PendingEntry pendingEntry where pendingEntry.id = :id and pendingEntry.userId = :userId")
    Optional<PendingEntry> findForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select distinct pendingEntry.entryDate from PendingEntry pendingEntry where pendingEntry.userId = :userId and pendingEntry.status = :status")
    List<LocalDate> findEntryDatesByStatus(@Param("userId") Long userId, @Param("status") PendingStatus status);

    @Query("select sum(pendingEntry.totalAmount) from PendingEntry pendingEntry " + "where pendingEntry.userId = :userId and pendingEntry.status = :status and pendingEntry.flow = :flow and pendingEntry.entryDate = :day")
    BigDecimal sumTotalByStatusAndFlowAndDay(@Param("userId") Long userId,
                                             @Param("status") PendingStatus status,
                                             @Param("flow")FlowDirection flow,
                                             @Param("day") LocalDate day);

    long countByUserIdAndStatusAndEntryDate(Long userId, PendingStatus status,  LocalDate entryDate);
}
