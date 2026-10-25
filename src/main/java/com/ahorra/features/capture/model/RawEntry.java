package com.ahorra.features.capture.model;

import com.ahorra.features.capture.enums.CaptureStatus;
import com.ahorra.features.capture.enums.EntrySource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "raw_entries")
public class RawEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 10)
    private EntrySource source;

    @Column(name = "raw_text", nullable = false, length = 2000)
    private String rawText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CaptureStatus status = CaptureStatus.RECEIVED;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "client_request_id", length = 100)
    private String clientRequestId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
