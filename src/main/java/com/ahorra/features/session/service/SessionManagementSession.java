package com.ahorra.features.session.service;

import com.ahorra.features.session.dto.InvalidateSessionResponse;

public interface SessionManagementSession {
    void createSession(Long userId, String token);
    void invalidateSession(String token);
    InvalidateSessionResponse invalidateUserSessions(Long userId);
    void updateSessionOnRefresh(Long userId, String newToken);
}
