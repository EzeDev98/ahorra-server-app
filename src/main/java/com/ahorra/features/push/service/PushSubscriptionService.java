package com.ahorra.features.push.service;

import com.ahorra.features.push.dto.PushSubscriptionRequest;

public interface PushSubscriptionService {
    void subscribe(Long userId, PushSubscriptionRequest request);
    void unsubscribe(Long userId, String endpoint);
    boolean isReminderEnabled(Long userId);
    void setReminderEnabled(Long userId, boolean enabled);
}
