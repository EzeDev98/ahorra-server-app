package com.ahorra.features.push.repository;

import com.ahorra.features.push.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByEndpoint(String endpoint);
    List<PushSubscription> findAllByUserIdOrderByIdAsc(Long userId);
    void deleteByEndpointAndUserId(String endpoint, Long userId);
}
